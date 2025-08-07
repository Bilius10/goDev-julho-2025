package br.com.senior.transport_logistics.dto.OpenRouteDTO.request;

public record RestrictionsRecord(
        double height,
        double weight,
        double length,
        boolean hazmat
) {
}
