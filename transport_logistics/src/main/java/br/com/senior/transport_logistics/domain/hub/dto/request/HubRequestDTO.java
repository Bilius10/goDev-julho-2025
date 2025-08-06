package br.com.senior.transport_logistics.domain.hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record HubRequestDTO(

        @NotBlank(message = "{hub.name.notBlank}")
        @Size(max = 100, message = "{hub.name.size}")
        String name,

        @NotBlank(message = "{hub.cnpj.notBlank}")
        @Pattern(message = "{hub.cnpj.pattern}", regexp = "^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$\n")
        String cnpj,

        @NotBlank(message = "{hub.cep.notBlank}")
        @Pattern(message = "{hub.cep.pattern}", regexp = "^(0[1-9]\\d{3}|1\\d{4})?\\d{3}$")
        String cep,

        @NotBlank(message = "{hub.number.notBlank}")
        @Size(max = 6, message = "{hub.number.size}")
        String number
) {
}
