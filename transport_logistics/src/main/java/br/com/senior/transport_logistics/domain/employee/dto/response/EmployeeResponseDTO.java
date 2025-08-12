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
        Long id,
        String name,
        String cnh,
        String cpf,
        String email,
        Boolean active,
        Role role,
        HubResponseDTO hub,
        String token
) {

    public static EmployeeResponseDTO basic(EmployeeEntity entity) {
        return EmployeeResponseDTO
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .cnh(entity.getCnh())
                .cpf(entity.getCpf())
                .email(entity.getEmail())
                .role(entity.getRole())
                .hub(HubResponseDTO.basic(entity.getHub()))
                .build();
    }

    public static EmployeeResponseDTO token(String token) {
        return EmployeeResponseDTO.builder()
                .token(token)
                .build();
    }
}