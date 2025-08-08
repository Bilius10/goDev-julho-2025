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
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import br.com.senior.transport_logistics.domain.truck.TruckService;
import br.com.senior.transport_logistics.domain.truck.dto.response.AverageDimensionsTrucks;
import br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO.GeminiResponse;
import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.ResponseForGemini;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.request.RestrictionsRecord;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import br.com.senior.transport_logistics.infrastructure.external.GeminiApiClientService;
import br.com.senior.transport_logistics.infrastructure.external.OpenRouteApiClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    public PageDTO<TransportResponseDTO> findAll(Pageable pageable){
        Page<TransportEntity> shipments = repository.findAll(pageable);

        Page<TransportResponseDTO> dtosPage = shipments.map(TransportResponseDTO::basic);

        return new PageDTO<>(
                dtosPage.getContent(),
                shipments.getNumber(),
                shipments.getSize(),
                shipments.getTotalElements(),
                shipments.getTotalPages());
    }

    public Mono<GeminiResponse> create(CreateTransportRequest request){
        AverageDimensionsTrucks averageDimensions = truckService.findAverageDimensionsTrucks();

        HubEntity originHub = hubService.findById(request.idOriginHub());
        HubEntity destinationHub = hubService.findById(request.idDestinationHub());

        ResponseForGemini route = openRouteApiClientService.obterDistancia(
                new CoordinatesDTO(originHub.getLongitude(), originHub.getLatitude()),
                new CoordinatesDTO(destinationHub.getLongitude(), destinationHub.getLatitude()),
                new RestrictionsRecord(averageDimensions.heightAvarege(), averageDimensions.weightAvarege(),
                        averageDimensions.lengthAvarege(), request.isHazmat()));

        ShipmentEntity shipment = shipmentService.findById(request.idShipment());

        List<TruckEntity> trucks = truckService.findByLoadCapacityGreaterThan(shipment.getWeight());

        Mono<GeminiResponse> geminiResponseMono = geminiApiClientService.chooseBetterComputer(route.steps().toString(), shipment, trucks);

        return geminiResponseMono;
    }

    public TransportResponseDTO update(UpdateTransportRequest request, Long id){
        TransportEntity transportFound = this.findById(id);

        EmployeeEntity employeeFound = employeeService.findById(request.employeeId());

        transportFound.updateTransport(request, employeeFound);

        TransportEntity saveTransport = repository.save(transportFound);

        return TransportResponseDTO.detailed(saveTransport);
    }

    public void delete(Long id){
        if(!repository.existsById(id)){
            throw new RuntimeException("Transporte não existe");
        }

        repository.deleteById(id);
    }

    private TransportEntity findById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transporte não encontrado"));
    }
}