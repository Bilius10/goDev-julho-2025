package br.com.senior.transport_logistics.domain.employee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeLoginRequestDTO(
        @Size(max = 100, message = "{employee.email.size}")
        @NotBlank(message = "{employee.email.notBlank}")
        @Email(message = "{employee.email.format}")
        String email,

        @Size(min = 8, max = 100, message = "{employee.password.size}")
        @NotBlank(message = "{employee.password.notBlank}")
        String password

) {

}
