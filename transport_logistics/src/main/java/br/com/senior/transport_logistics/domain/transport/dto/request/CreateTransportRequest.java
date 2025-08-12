package br.com.senior.transport_logistics.domain.transport.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateTransportRequest(

        @NotNull(message = "{transport.exitDay.notNull}")
        @Future(message = "{transport.request.exitDay.future}")
        LocalDate exitDay,

        @NotNull(message = "{transport.request.idShipment.notNull}")
        @Positive(message = "{transport.request.idShipment.positive}")
        Long idShipment,

        @NotNull(message = "{transport.request.idOriginHub.notNull}")
        @Positive(message = "{transport.request.idOriginHub.positive}")
        Long idOriginHub,

        @NotNull(message = "{transport.request.idDestinationHub.notNull}")
        @Positive(message = "{transport.request.idDestinationHub.positive}")
        Long idDestinationHub,

        @NotNull(message = "{transport.request.isHazmat.notNull}")
        boolean isHazmat
) {
}