package br.com.senior.transport_logistics.dto;

import java.util.List;

public record PageDTO<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        long totalPages
) {
}
