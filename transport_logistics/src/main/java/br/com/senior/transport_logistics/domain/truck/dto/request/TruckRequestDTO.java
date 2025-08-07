package br.com.senior.transport_logistics.domain.truck.dto.request;

import br.com.senior.transport_logistics.domain.truck.enums.AxleSetup;
import br.com.senior.transport_logistics.domain.truck.enums.TruckBody;
import br.com.senior.transport_logistics.domain.truck.enums.TruckType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TruckRequestDTO(
        @NotBlank(message = "{truck.code.notBlank}")
        @Size(max = 50, message = "{truck.code.size}")
        String code,

        @NotBlank(message = "{truck.model.notBlank}")
        @Size(max = 100, message = "{truck.model.size}")
        String model,

        @NotNull(message = "{truck.type.notNull}")
        TruckType type,

        @NotNull(message = "{truck.body.notNull}")
        TruckBody body,

        @NotNull(message = "{truck.axleSetup.notNull}")
        AxleSetup axleSetup,

        @NotNull(message = "{truck.loadCapacity.notNull}")
        @Positive(message = "{truck.loadCapacity.positive}")
        Double loadCapacity,

        @NotNull(message = "{truck.weight.notNull}")
        @Positive(message = "{truck.weight.positive}")
        Double weight,

        @NotNull(message = "{truck.length.notNull}")
        @Positive(message = "{truck.length.positive}")
        Double length,

        @NotNull(message = "{truck.width.notNull}")
        @Positive(message = "{truck.width.positive}")
        Double width,

        @NotNull(message = "{truck.height.notNull}")
        @Positive(message = "{truck.height.positive}")
        Double height,

        @NotNull(message = "{truck.averageFuelConsumption.notNull}")
        @Positive(message = "{truck.averageFuelConsumption.positive}")
        Double averageFuelConsumption,

        @Size(max = 100, message = "{truck.features.size}")
        String features
) {
}
