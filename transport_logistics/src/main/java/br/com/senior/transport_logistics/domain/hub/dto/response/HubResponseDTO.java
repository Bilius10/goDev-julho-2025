package br.com.senior.transport_logistics.domain.hub.dto.response;

import br.com.senior.transport_logistics.domain.hub.HubEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HubResponseDTO(
        Long id,
        String name,
        String cnpj,
        String street,
        String number,
        String district,
        String city,
        String state,
        String country,
        Double latitude,
        Double longitude,
        String cep
) {

    public static HubResponseDTO basic(HubEntity entity) {
        return HubResponseDTO
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .cnpj(entity.getCnpj())
                .number(entity.getNumber())
                .cep(entity.getCep())
                .build();
    }

    public static HubResponseDTO detailed(HubEntity entity) {
        return HubResponseDTO
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .cnpj(entity.getCnpj())
                .street(entity.getStreet())
                .number(entity.getNumber())
                .district(entity.getDistrict())
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .cep(entity.getCep())
                .build();

    }
}
