package br.com.senior.transport_logistics.domain.hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record HubUpdateRequestDTO(
        @NotBlank(message = "{hub.name.notBlank}")
        @Size(max = 100, message = "{hub.name.size}")
        String name,

        @NotBlank(message = "{hub.cep.notBlank}")
        @Pattern(message = "{hub.cep.pattern}", regexp = "^\\d{8}$")
        String cep,

        @NotBlank(message = "{hub.number.notBlank}")
        @Size(max = 6, message = "{hub.number.size}")
        String number
) {
}
