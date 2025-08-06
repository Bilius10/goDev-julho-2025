package br.com.senior.transport_logistics.domain.employee.dto.response;

import br.com.senior.transport_logistics.domain.employee.enums.Role;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeResponseDTO(
        long id,
        String name,
        String cnh,
        String cpf,
        String email,
        boolean active,
        Role role,
        HubResponseDTO hub
) {
}