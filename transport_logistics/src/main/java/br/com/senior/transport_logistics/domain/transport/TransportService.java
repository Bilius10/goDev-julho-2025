package br.com.senior.transport_logistics.domain.transport;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.EmployeeService;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import br.com.senior.transport_logistics.domain.shipment.ShipmentService;
import br.com.senior.transport_logistics.domain.transport.dto.request.CreateTransportRequest;
import br.com.senior.transport_logistics.domain.transport.dto.request.UpdateTransportRequest;
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
import br.com.senior.transport_logistics.infrastructure.external.GeminiApiClientService;
import br.com.senior.transport_logistics.infrastructure.external.OpenRouteApiClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

    public TransportResponseDTO optimizeAllocation(CreateTransportRequest request) throws JsonProcessingException {
        HubEntity originHub = hubService.findById(request.idOriginHub());
        HubEntity destinationHub = hubService.findById(request.idDestinationHub());
        ShipmentEntity shipment = shipmentService.findById(request.idShipment());

        ResponseForGemini route = getRouteData(originHub, destinationHub, shipment, request.isHazmat());

        long travelDays = (long) Math.ceil(route.duration() / 86400.0);

        LocalDate availabilityDeadline = request.exitDay().plusDays(travelDays * 2);

        GeminiResponse truckSuggestion = selectIdealTruck(request, shipment, originHub, route, availabilityDeadline);

        TruckEntity chosenTruck = truckService.findById(truckSuggestion.caminhaoSugerido());

        EmployeeEntity chosenDriver = employeeService.findDriversOrderedByHistoryScore(
                truckSuggestion.caminhaoSugerido(),
                destinationHub.getId(),
                originHub.getId()
        );

        TransportEntity newTransport = new TransportEntity(
                chosenDriver, originHub, destinationHub, shipment,
                chosenTruck, route.distance(), truckSuggestion.litrosGastos(), request.exitDay(),
                availabilityDeadline
        );

        TransportEntity savedTransport = repository.save(newTransport);

        return TransportResponseDTO.geminiResponse(savedTransport, truckSuggestion.justificativa());
    }

    public TransportResponseDTO confirmTransport(Long id){
        TransportEntity transport = this.findById(id);
        transport.setStatus(TransportStatus.ASSIGNED);

        TransportEntity savedTransport = repository.save(transport);
        emailService.sendConfirmTransportEmail(savedTransport);

        return TransportResponseDTO.basic(savedTransport);
    }

    public TransportResponseDTO updateStatus(Long id, TransportStatus status){
        TransportEntity transport = this.findById(id);
        transport.setStatus(status);

        TransportEntity savedTransport = repository.save(transport);

        return TransportResponseDTO.basic(savedTransport);
    }

    public TransportResponseDTO update(UpdateTransportRequest request, Long id) {
        TransportEntity transportToUpdate = this.findById(id);
        EmployeeEntity assignedEmployee = employeeService.findById(request.employeeId());

        transportToUpdate.updateTransport(request, assignedEmployee);

        TransportEntity updatedTransport = repository.save(transportToUpdate);

        return TransportResponseDTO.detailed(updatedTransport);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Transport not found with id: " + id);
        }
        repository.deleteById(id);
    }


    private TransportEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transport not found with id: " + id));
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
                                             HubEntity originHub, ResponseForGemini route, LocalDate availabilityDeadline) throws JsonProcessingException {

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
                candidateTrucks
        );
    }
}
