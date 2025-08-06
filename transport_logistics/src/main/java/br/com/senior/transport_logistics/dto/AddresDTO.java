package br.com.senior.transport_logistics.dto;

public record AddresDTO(
        String logradouro,
        String bairro,
        String localidade,
        String uf
) {
}
