package br.com.senior.transport_logistics.domain.transport.dto.request;

import br.com.senior.transport_logistics.domain.transport.enums.TransportStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateTransportRequest(
        @NotNull(message = "{transport.exitDay.notNull}")
        @Future(message = "{transport.exitDay.future}")
        LocalDate exitDay,

        @NotNull(message = "{transport.driver.notNull}")
        Long employeeId,

        @NotNull(message = "{transport.status.notNull}")
        @Column(name = "status", length = 50)
        TransportStatus status
        ) {
}
