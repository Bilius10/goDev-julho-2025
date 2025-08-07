package br.com.senior.transport_logistics.domain.truck.dto.response;

import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TruckResponseDTO(
        String code,
        String model,
        HubResponseDTO hub,
        String type,
        String body,
        String axleSetup,
        Double loadCapacity,
        Double weight,
        Double length,
        Double width,
        Double height,
        Double averageFuelConsumption,
        String status,
        String features
) {

    public static TruckResponseDTO basic(TruckEntity truck) {
        return TruckResponseDTO.builder()
                .code(truck.getCode())
                .model(truck.getModel())
                .type(truck.getType().getDescription())
                .loadCapacity(truck.getLoadCapacity())
                .status(truck.getStatus().getDescription())
                .build();
    }

    public static TruckResponseDTO detailed(TruckEntity truck) {
        return TruckResponseDTO.builder()
                .code(truck.getCode())
                .model(truck.getModel())
                .hub(HubResponseDTO.basic(truck.getHub()))
                .type(truck.getType().getDescription())
                .body(truck.getBody().getDescription())
                .axleSetup(truck.getAxleSetup().getDescription())
                .loadCapacity(truck.getLoadCapacity())
                .weight(truck.getWeight())
                .length(truck.getLength())
                .width(truck.getWidth())
                .height(truck.getHeight())
                .averageFuelConsumption(truck.getAverageFuelConsumption())
                .status(truck.getStatus().getDescription())
                .features(truck.getFeatures())
                .build();
    }

}
