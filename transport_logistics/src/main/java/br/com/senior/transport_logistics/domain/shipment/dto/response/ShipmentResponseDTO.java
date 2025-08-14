package br.com.senior.transport_logistics.domain.shipment.dto.response;

import br.com.senior.transport_logistics.domain.product.ProductEntity;
import br.com.senior.transport_logistics.domain.product.dto.response.ProductResponseDTO;
import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShipmentResponseDTO(
        Long id,
        Double weight,
        Integer quantity,
        String notes,
        String productName,
        boolean isHazardous
) {

    public static ShipmentResponseDTO detailed(ShipmentEntity entity) {
        return ShipmentResponseDTO
                .builder()
                .id(entity.getId())
                .weight(entity.getWeight())
                .quantity(entity.getQuantity())
                .notes(entity.getNotes())
                .productName(entity.getProduct().getName())
                .isHazardous(entity.isHazardous())
                .build();

    }
}
