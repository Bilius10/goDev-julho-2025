package br.com.senior.transport_logistics.infrastructure.dto.ViaCepDTO;

public record AddresDTO(
        String logradouro,
        String bairro,
        String localidade,
        String uf
) {
}
