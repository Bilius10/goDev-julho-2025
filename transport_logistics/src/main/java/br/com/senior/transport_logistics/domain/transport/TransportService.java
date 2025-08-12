package br.com.senior.transport_logistics.domain.transport;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.EmployeeService;
import br.com.senior.transport_logistics.domain.employee.enums.Role;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import br.com.senior.transport_logistics.domain.shipment.ShipmentService;
import br.com.senior.transport_logistics.domain.transport.dto.request.CreateTransportRequest;
import br.com.senior.transport_logistics.domain.transport.dto.request.UpdateTransportRequest;
import br.com.senior.transport_logistics.domain.transport.dto.response.HubSummaryProjection;
import br.com.senior.transport_logistics.domain.transport.dto.response.TransportCreatedResponseDTO;
import br.com.senior.transport_logistics.domain.transport.dto.response.TransportResponseDTO;
import br.com.senior.transport_logistics.domain.transport.enums.TransportStatus;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import br.com.senior.transport_logistics.domain.truck.TruckService;
import br.com.senior.transport_logistics.domain.truck.dto.response.AverageDimensionsTrucks;
import br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO.GeminiResponse;
import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.ResponseForGemini;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.request.RestrictionsRecord;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import br.com.senior.transport_logistics.infrastructure.email.SpringMailSenderService;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import br.com.senior.transport_logistics.infrastructure.external.GeminiApiClientService;
import br.com.senior.transport_logistics.infrastructure.external.OpenRouteApiClientService;
import br.com.senior.transport_logistics.infrastructure.pdf.PdfGenerationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.HUB_NOT_FOUND_BY_ID;
import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.TRANSPORT_NOT_FOUND_BY_ID;

@Service
@RequiredArgsConstructor
public class TransportService {

    private final TransportRepository repository;
    private final TruckService truckService;
    private final HubService hubService;
    private final ShipmentService shipmentService;
    private final OpenRouteApiClientService openRouteApiClientService;
    private final GeminiApiClientService geminiApiClientService;
    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;
    private final SpringMailSenderService emailService;
    private final PdfGenerationService pdfGenerationService;

    @Transactional(readOnly = true)
    public PageDTO<TransportResponseDTO> findAll(Pageable pageable) {

        Page<TransportEntity> transportPage = repository.findAll(pageable);

        Page<TransportResponseDTO> dtoPage = transportPage.map(TransportResponseDTO::basic);

        return new PageDTO<>(
                dtoPage.getContent(),
                transportPage.getNumber(),
                transportPage.getSize(),
                transportPage.getTotalElements(),
                transportPage.getTotalPages());
    }

    public HubSummaryProjection hubSummary(Long id){
        return repository.findHubSummaryById(id)
                .orElseThrow(() -> new ResourceNotFoundException(HUB_NOT_FOUND_BY_ID.getMessage(id)));
    }

    @Transactional
    public TransportCreatedResponseDTO optimizeAllocation(CreateTransportRequest request) throws JsonProcessingException {
        HubEntity originHub = hubService.findById(request.idOriginHub());
        HubEntity destinationHub = hubService.findById(request.idDestinationHub());
        ShipmentEntity shipment = shipmentService.findById(request.idShipment());

        List<ShipmentEntity> pendingShipments
                = shipmentService.findAllByIdHubAndDestinationHubAndStatus(TransportStatus.PENDING, request.idDestinationHub(), request.idOriginHub());

        ResponseForGemini route = getRouteData(originHub, destinationHub, shipment, request.isHazmat());

        long travelDays = (long) Math.ceil(route.duration() / 86400.0);

        LocalDate availabilityDeadline = request.exitDay().plusDays(travelDays);

        GeminiResponse truckSuggestion
                = this.selectIdealTruck(request, shipment, originHub, route, availabilityDeadline, pendingShipments);

        TruckEntity chosenTruck = truckService.findById(truckSuggestion.caminhaoSugerido());

        EmployeeEntity chosenDriver = employeeService.findDriversOrderedByHistoryScore(
                truckSuggestion.caminhaoSugerido(),
                destinationHub.getId(),
                originHub.getId()
        );

        double fuel = truckSuggestion.litrosGastosIda();

        TransportEntity exitTransport = new TransportEntity(
                chosenDriver, originHub, destinationHub, shipment,
                chosenTruck, route.distance(), truckSuggestion.litrosGastosIda(), request.exitDay(),
                availabilityDeadline
        );
        repository.save(exitTransport);

        if (truckSuggestion.produtoSelecionadoRetorno() != null) {
            fuel += truckSuggestion.litrosGastosVolta();

            ShipmentEntity shipmentReturn = shipmentService.findById(request.idShipment());

            TransportEntity returnTransport = new TransportEntity(
                    chosenDriver, destinationHub, originHub, shipmentReturn,
                    chosenTruck, route.distance()/2, truckSuggestion.litrosGastosVolta(), null,
                    availabilityDeadline
            );
            repository.save(returnTransport);
        }
        
        return TransportCreatedResponseDTO.buildCreatedResponse(exitTransport, truckSuggestion.justificativaCaminhao(), 
                truckSuggestion.justificativaCargaRetorno(), fuel, route.distance());
    }

    @Transactional
    public TransportResponseDTO confirmTransport(Long id){
        TransportEntity transport = this.findById(id);
        transport.setStatus(TransportStatus.ASSIGNED);

        TransportEntity savedTransport = repository.save(transport);

        byte[] bytes = pdfGenerationService.generateTransportManifestPdf(savedTransport);

        emailService.sendConfirmTransportEmail(savedTransport, bytes);

        return TransportResponseDTO.detailed(savedTransport);
    }

    @Scheduled(cron = "0 0 9 * * SAT")
    @Transactional(readOnly = true)
    public void sendWeeklySchedule(){
        List<TransportEntity> weeklyTransport
                = repository.findAllByExitDay(LocalDate.now(), LocalDate.now().plusDays(6));

        Map<EmployeeEntity, List<TransportEntity>> transportsByDriver = weeklyTransport.stream()
                .collect(Collectors.groupingBy(TransportEntity::getDriver));

        transportsByDriver.forEach((driver, driverTransports) -> {
            emailService.sendWeeklyScheduleEmail(driverTransports);
        });
    }

    @Scheduled(cron = "0 0 9 1 * ?")
    @Transactional(readOnly = true)
    public void sendMonthReport() {
        List<EmployeeEntity> managers = employeeService.findAllByRole(Role.MANAGER);

        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        LocalDate startDate = previousMonth.atDay(1);
        LocalDate endDate = previousMonth.atEndOfMonth();

        for (EmployeeEntity manager : managers) {
            HubEntity hub = manager.getHub();

            List<TransportEntity> monthlyTransports = repository.findAllByExitDayAndOriginHub(LocalDate.now(), LocalDate.now().plusDays(8), hub.getId());
            List<EmployeeEntity> hubDrivers = employeeService.findAllByRoleAndHub(Role.DRIVER, hub);
            List<TruckEntity> hubTrucks = truckService.findAllByHub(hub);

            double totalDistance = monthlyTransports.stream()
                    .filter(transport -> transport.getDistance() != null)
                    .filter(transport -> transport.getStatus() == TransportStatus.DELIVERED)
                    .mapToDouble(TransportEntity::getDistance)
                    .sum();

            Map<String, Double> fuelByTruck = monthlyTransports.stream()
                    .filter(month -> month.getStatus() == TransportStatus.DELIVERED)
                    .collect(Collectors.groupingBy(
                            transport -> transport.getTruck().getModel(),
                            Collectors.summingDouble(TransportEntity::getFuelConsumption)
                    ));

            emailService.sendMonthReportEmail(manager, monthlyTransports, hubDrivers, hubTrucks, fuelByTruck, totalDistance);
        }

    }

    @Transactional
    public TransportResponseDTO updateStatus(Long id, TransportStatus status){
        TransportEntity transport = this.findById(id);
        transport.setStatus(status);
        transport.getShipment().setStatus(status);

        TransportEntity savedTransport = repository.save(transport);

        return TransportResponseDTO.basic(savedTransport);
    }

    @Transactional
    public TransportResponseDTO update(UpdateTransportRequest request, Long id) {
        TransportEntity transportToUpdate = this.findById(id);
        EmployeeEntity assignedEmployee = employeeService.findById(request.employeeId());

        transportToUpdate.updateTransport(request, assignedEmployee);

        TransportEntity updatedTransport = repository.save(transportToUpdate);

        return TransportResponseDTO.detailed(updatedTransport);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(TRANSPORT_NOT_FOUND_BY_ID.getMessage(id));
        }
        repository.deleteById(id);
    }

    private TransportEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRANSPORT_NOT_FOUND_BY_ID.getMessage(id)));
    }

    private ResponseForGemini getRouteData(HubEntity originHub, HubEntity destinationHub, ShipmentEntity shipment, boolean isHazmat) {
        AverageDimensionsTrucks averageDimensions = truckService.findAverageDimensionsTrucks();

        RestrictionsRecord restrictions = new RestrictionsRecord(
                averageDimensions.heightAvarege(),
                averageDimensions.weightAvarege() + shipment.getWeight(),
                averageDimensions.lengthAvarege(),
                isHazmat
        );

        return openRouteApiClientService.obterDistancia(
                new CoordinatesDTO(originHub.getLongitude(), originHub.getLatitude()),
                new CoordinatesDTO(destinationHub.getLongitude(), destinationHub.getLatitude()),
                restrictions
        );
    }

    private GeminiResponse selectIdealTruck(CreateTransportRequest request, ShipmentEntity shipment,
                                             HubEntity originHub, ResponseForGemini route, LocalDate availabilityDeadline,
                                            List<ShipmentEntity> pendingShipments ) throws JsonProcessingException {

        List<TruckEntity> candidateTrucks = truckService.findByLoadCapacityGreaterThan(
                shipment.getWeight(),
                originHub.getId(),
                request.exitDay(),
                availabilityDeadline
        );

        String routeStepsJson = objectMapper.writeValueAsString(route.steps());

        return geminiApiClientService.chooseBestTruck(
                routeStepsJson,
                route.distance(),
                shipment,
                candidateTrucks,
                pendingShipments
        );
    }
}
