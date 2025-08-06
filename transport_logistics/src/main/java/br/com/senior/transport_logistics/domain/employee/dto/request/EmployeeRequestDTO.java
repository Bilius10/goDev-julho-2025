package br.com.senior.transport_logistics.domain.employee.dto.request;

import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.employee.enums.Role;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

public record EmployeeRequestDTO(
        @Size(max = 100, message = "{employee.name.size}")
        @NotBlank(message = "{employee.name.notBlank}")
        String name,

        @Size(max = 11, message = "{employee.cnh.size}")
        @NotBlank(message = "{employee.cnh.notBlank}")
        @Pattern(regexp = "^\\d{11}$", message = "CNH deve conter 11 dígitos numéricos")
        String cnh,

        @Size(max = 11, message = "{employee.cpf.size}")
        @CPF
        String cpf,

        @Size(max = 100, message = "{employee.email.size}")
        @NotBlank(message = "{employee.email.notBlank}")
        @Email
        String email,

        HubEntity hub,

        @NotNull(message = "{employee.role.notNull}")
        @Size(max = 50, message = "{employee.role.size}")
        Role role
) {
}
