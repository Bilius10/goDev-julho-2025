package br.com.senior.transport_logistics.domain.employee.dto.request;

import br.com.senior.transport_logistics.domain.employee.enums.Role;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

public record EmployeeCreateRequestDTO(
        @Size(max = 100, message = "{employee.name.size}")
        @NotBlank(message = "{employee.name.notBlank}")
        String name,

        @Size(max = 11, message = "{employee.cnh.size}")
        @NotBlank(message = "{employee.cnh.notBlank}")
        @Pattern(regexp = "^\\d{11}$", message = "{employee.cnh.format}")
        String cnh,

        @CPF(message = "{employee.cpf.format}")
        String cpf,

        @Size(max = 100, message = "{employee.email.size}")
        @NotBlank(message = "{employee.email.notBlank}")
        @Email(message = "{employee.email.format}")
        String email,

        @Size(min = 8, max = 100, message = "{employee.password.size}")
        @NotBlank(message = "{employee.password.notBlank}")
        String password,

        @NotNull(message = "{employee.idHub.notNull}")
        @Positive(message = "{employee.idHub.Positive}")
        Long idHub,

        @NotNull(message = "{employee.role.notNull}")
        Role role
) {
}
