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
import br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO.TransportRecommendation;
import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.ORSRoute;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
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

    // Self-injection para contornar limitacao do proxy transacional do Spring
    // Chamadas internas (this.metodo()) nao passam pelo proxy, ignorando @Transactional
    // Self-injection permite que chamadas internas sejam interceptadas pelo proxy
    private TransportService self;

    @Autowired
    @Lazy // Evita dependencia circular - injeta proxy lazy ao inves do bean real
    public void setSelf(TransportService self) {
        this.self = self;
    }

    private static final double SECONDS_IN_A_DAY = 86400.0;

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

    public HubSummaryProjection hubSummary(Long id) {
        return repository.findHubSummaryById(id)
                .orElseThrow(() -> new ResourceNotFoundException(HUB_NOT_FOUND_BY_ID.getMessage(id)));
    }

    // anteriormente fazia tudo, agora delega para outros metodos
    public TransportCreatedResponseDTO optimizeAllocation(CreateTransportRequest request) throws JsonProcessingException {
        TransportAllocationData allocationData = prepareAllocationData(request);

        TransportEntity exitTransport = self.createTransports(allocationData);

        return TransportCreatedResponseDTO.buildCreatedResponse(
                exitTransport,
                allocationData.truckSuggestion().truckJustification(),
                allocationData.truckSuggestion().returnShipmentJustification(),
                allocationData.totalFuel(),
                allocationData.route().distance()
        );
    }

    // Obtem todos os dados necessarios para criar o transporte (do banco de dados e das APIs)
    private TransportAllocationData prepareAllocationData(CreateTransportRequest request) throws JsonProcessingException {
        HubEntity originHub = hubService.findById(request.idOriginHub());
        HubEntity destinationHub = hubService.findById(request.idDestinationHub());
        ShipmentEntity shipment = shipmentService.findById(request.idShipment());

        List<ShipmentEntity> pendingShipments = getPendingShipments(request);

        ORSRoute route = getRouteData(originHub, destinationHub, shipment, request.isHazmat());
        LocalDate availabilityDeadline = calculateAvailabilityDeadline(request, route);

        TransportRecommendation truckSuggestion = selectIdealTruck(request, shipment, originHub, route, availabilityDeadline, pendingShipments);


        TruckEntity chosenTruck = truckService.findById(truckSuggestion.suggestedTruckId());
        EmployeeEntity chosenDriver = getChosenDriver(truckSuggestion, destinationHub, originHub);

        double totalFuel = truckSuggestion.litersSpentOutbound();
        ShipmentEntity returnShipment = null;

        if (truckSuggestion.returnShipmentId() != null) {
            totalFuel += truckSuggestion.litersSpentReturn();
            returnShipment = shipmentService.findById(truckSuggestion.returnShipmentId());
        }

        return new TransportAllocationData(
                originHub, destinationHub, shipment, returnShipment,
                chosenDriver, chosenTruck, route, truckSuggestion,
                availabilityDeadline, totalFuel, request
        );
    }

    @Transactional
    public TransportEntity createTransports(TransportAllocationData data) {
        TransportEntity exitTransport = createExitTransport(data);
        repository.save(exitTransport);

        if (data.returnShipment() != null) {
            TransportEntity returnTransport = createReturnTransport(data);
            repository.save(returnTransport);
        }

        return exitTransport;
    }

    private TransportEntity createExitTransport(TransportAllocationData data) {
        return new TransportEntity(
                data.chosenDriver(), data.originHub(), data.destinationHub(), data.shipment(),
                data.chosenTruck(), data.route().distance(), data.truckSuggestion().litersSpentOutbound(),
                data.request().exitDay(), data.availabilityDeadline()
        );
    }

    private TransportEntity createReturnTransport(TransportAllocationData data) {
        return new TransportEntity(
                data.chosenDriver(), data.destinationHub(), data.originHub(), data.returnShipment(),
                data.chosenTruck(), data.route().distance() / 2, data.truckSuggestion().litersSpentReturn(),
                null, data.availabilityDeadline()
        );
    }

    private List<ShipmentEntity> getPendingShipments(CreateTransportRequest request) {
        return shipmentService.findAllByIdHubAndDestinationHubAndStatus(
                TransportStatus.PENDING,
                request.idDestinationHub(),
                request.idOriginHub()
        );
    }

    private LocalDate calculateAvailabilityDeadline(CreateTransportRequest request, ORSRoute route) {
        long travelDays = getTravelDays(route.duration());
        return request.exitDay().plusDays(travelDays);
    }

    private EmployeeEntity getChosenDriver(TransportRecommendation truckSuggestion, HubEntity destinationHub, HubEntity originHub) {
        return employeeService.findDriversOrderedByHistoryScore(
                truckSuggestion.suggestedTruckId(),
                destinationHub.getId(),
                originHub.getId()
        );
    }

    @Transactional
    public TransportResponseDTO confirmTransport(Long id) {
        TransportEntity transport = this.findById(id);
        transport.setStatus(TransportStatus.ASSIGNED);

        TransportEntity savedTransport = repository.save(transport);

        byte[] bytes = pdfGenerationService.generateTransportManifestPdf(savedTransport);

        emailService.sendConfirmTransportEmail(savedTransport, bytes);

        return TransportResponseDTO.detailed(savedTransport);
    }

    @Scheduled(cron = "0 0 9 * * SAT")
    @Transactional(readOnly = true)
    public void sendWeeklySchedule() {
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
    public TransportResponseDTO updateStatus(Long id, TransportStatus status) {
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

    private ORSRoute getRouteData(HubEntity originHub, HubEntity destinationHub, ShipmentEntity shipment, boolean isHazmat) {
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

    private TransportRecommendation selectIdealTruck(CreateTransportRequest request, ShipmentEntity shipment,
                                                     HubEntity originHub, ORSRoute route, LocalDate availabilityDeadline,
                                                     List<ShipmentEntity> pendingShipments) throws JsonProcessingException {

        List<TruckEntity> candidateTrucks = truckService.findByLoadCapacityGreaterThan(
                shipment.getWeight(),
                originHub.getId(),
                request.exitDay(),
                availabilityDeadline
        );

        String routeStepsJson = objectMapper.writeValueAsString(route.steps());

        System.out.println(routeStepsJson);
        System.out.println(route.distance());
        System.out.println(shipment);
        System.out.println(candidateTrucks);
        System.out.println(pendingShipments);

        return geminiApiClientService.chooseBestTruck(
                routeStepsJson,
                route.distance(),
                shipment,
                candidateTrucks,
                pendingShipments
        );
    }

    private long getTravelDays(double routeDuration) {
        return (long) Math.ceil(routeDuration / SECONDS_IN_A_DAY);
    }
}
