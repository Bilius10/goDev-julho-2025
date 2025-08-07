package br.com.senior.transport_logistics.dto;

import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;

import java.util.List;
import java.util.stream.Stream;

public record PageDTO<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        long totalPages
) {

}
