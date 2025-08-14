package br.com.senior.transport_logistics.domain.transport.dto.response;

import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.domain.transport.TransportEntity;
import br.com.senior.transport_logistics.domain.truck.dto.response.TruckResponseDTO;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TransportCreatedResponseDTO(
        Long idTransport,
        Double totalFuelConsuption,
        Double totalDistance,
        LocalDate exitDay,
        LocalDate expectedArrivalDay,
        String truckModel,
        String ProductExitName,
        String driverName,
        String originHubName,
        String destinationHubName,
        String responseChooseTruck,
        String responseChooseReturnProduct
) {

    public static TransportCreatedResponseDTO buildCreatedResponse(TransportEntity transportEntity,
                                                                   String responseChooseTruck,
                                                                   String responseChooseReturnProduct,
                                                                   Double totalFuelConsuption,
                                                                   Double totalDistance) {
        return TransportCreatedResponseDTO.builder()
                .idTransport(transportEntity.getId())
                .totalFuelConsuption(totalFuelConsuption)
                .totalDistance(totalDistance)
                .exitDay(transportEntity.getExitDay())
                .expectedArrivalDay(transportEntity.getExpectedArrivalDay())
                .truckModel(transportEntity.getTruck().getModel())
                .ProductExitName(transportEntity.getShipment().getProduct().getName())
                .driverName(transportEntity.getDriver().getName())
                .originHubName(transportEntity.getOriginHub().getName())
                .destinationHubName(transportEntity.getDestinationHub().getName())
                .responseChooseTruck(responseChooseTruck)
                .responseChooseReturnProduct(responseChooseReturnProduct)
                .build();
    }


}
