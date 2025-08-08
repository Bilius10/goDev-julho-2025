package br.com.senior.transport_logistics.domain.transport.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateTransportRequest(

        @NotNull(message = "{transport.exitDay.notNull}")
        @Future(message = "{transport.exitDay.future}")
        LocalDate exitDay,

        @NotNull(message = "{transport.idShipment.notNull}")
        @Positive(message = "{transport.idShipment.positive}")
        Long idShipment,

        @NotNull(message = "{transport.idOriginHub.notNull}")
        @Positive(message = "{transport.idOriginHub.positive}")
        Long idOriginHub,

        @NotNull(message = "{transport.idDestinationHub.notNull}")
        @Positive(message = "{transport.idDestinationHub.positive}")
        Long idDestinationHub,

        @NotNull(message = "{transport.isHazmat.notNull}")
        boolean isHazmat
) {
}