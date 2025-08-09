package br.com.senior.transport_logistics.domain.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeePasswordUpdateDTO(
        @Size(min = 8, max = 100, message = "{employee.password.size}")
        @NotBlank(message = "{employee.password.notBlank}")
        String currentPassword,

        @Size(min = 8, max = 100, message = "{employee.password.size}")
        @NotBlank(message = "{employee.password.notBlank}")
        String newPassword,

        @Size(min = 8, max = 100, message = "{employee.password.size}")
        @NotBlank(message = "{employee.password.notBlank}")
        String confirmNewPassword
) {
}
