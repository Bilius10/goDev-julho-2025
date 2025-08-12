package br.com.senior.transport_logistics.domain.shipment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ShipmentCreateDTO(
        @NotNull(message = "{shipment.quantity.notNull}")
        @Min(value = 1, message = "{shipment.quantity.min}")
        Integer quantity,

        @Size(max = 100, message = "{shipment.notes.size}")
        String notes,

        @NotNull(message = "{shipment.isHazardous.notNull}")
        boolean isHazardous,

        @NotNull(message = "{shipment.product.notNull}")
        @Positive(message = "{shipment.request.idProduct.positive}")
        Long idProduct,

        long idOriginHub,

        long idDestinationHub
) {
}
