package br.com.senior.transport_logistics.domain.transport.dto.response;

import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.domain.transport.TransportEntity;
import br.com.senior.transport_logistics.domain.truck.dto.response.TruckResponseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransportResponseDTO(
        Long id,
        Double fuelConsumption,
        Double distance,
        LocalDate exitDay,
        LocalDate expectedArrivalDay,
        String status,
        String truckModel,
        Long shipmentTrackingCode,
        String driverName,
        String originHubName,
        String destinationHubName,
        HubResponseDTO originHub,
        HubResponseDTO destinationHub,
        TruckResponseDTO truck
) {

    public static TransportResponseDTO basic(TransportEntity entity) {
        return TransportResponseDTO.builder()
                .id(entity.getId())
                .status(entity.getStatus().getDescription())
                .exitDay(entity.getExitDay())
                .expectedArrivalDay(entity.getExpectedArrivalDay())
                .truckModel(entity.getTruck().getModel())
                .shipmentTrackingCode(entity.getShipment().getId())
                .build();
    }

    public static TransportResponseDTO detailed(TransportEntity entity) {

        return TransportResponseDTO.builder()
                .id(entity.getId())
                .fuelConsumption(entity.getFuelConsumption())
                .distance(entity.getDistance())
                .exitDay(entity.getExitDay())
                .expectedArrivalDay(entity.getExpectedArrivalDay())
                .status(entity.getStatus().getDescription())
                .truckModel(entity.getTruck().getModel())
                .shipmentTrackingCode(entity.getShipment().getId())
                .driverName(entity.getDriver().getName())
                .originHubName(entity.getOriginHub().getName())
                .destinationHubName(entity.getDestinationHub().getName())
                .build();
    }


}