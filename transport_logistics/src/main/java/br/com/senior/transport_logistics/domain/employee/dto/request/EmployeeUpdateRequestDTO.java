package br.com.senior.transport_logistics.domain.employee.dto.request;

import br.com.senior.transport_logistics.domain.employee.enums.Role;
import jakarta.validation.constraints.*;

public record EmployeeUpdateRequestDTO(
        @Size(max = 100, message = "{employee.name.size}")
        @NotBlank(message = "{employee.name.notBlank}")
        String name,

        @Size(max = 100, message = "{employee.email.size}")
        @NotBlank(message = "{employee.email.notBlank}")
        @Email(message = "{employee.email.format}")
        String email
) {
}
