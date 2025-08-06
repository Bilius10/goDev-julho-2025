package br.com.senior.transport_logistics.domain.hub.dto.response;

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

    public HubResponseDTO(Long id, String name, String cnpj, String cep, String number) {
        this(id, name, cnpj, null, number, null, null, null, null, null, null, cep);
    }

    public HubResponseDTO(Long id, String name, String cnpj, String street, String number, String district, String city, String state, String country, Double latitude, Double longitude, String cep) {
        this.id = id;
        this.name = name;
        this.cnpj = cnpj;
        this.street = street;
        this.number = number;
        this.district = district;
        this.city = city;
        this.state = state;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cep = cep;
    }
}
