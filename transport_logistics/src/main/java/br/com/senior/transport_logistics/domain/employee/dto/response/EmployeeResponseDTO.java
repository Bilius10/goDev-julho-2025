package br.com.senior.transport_logistics.domain.employee.dto.response;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.enums.Role;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
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

    public static EmployeeResponseDTO basic(EmployeeEntity entity, HubEntity hub) {
        return EmployeeResponseDTO
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .cnh(entity.getCnh())
                .cpf(entity.getCpf())
                .email(entity.getEmail())
                .role(entity.getRole())
                .hub(HubResponseDTO.basic(hub))
                .build();
    }
}