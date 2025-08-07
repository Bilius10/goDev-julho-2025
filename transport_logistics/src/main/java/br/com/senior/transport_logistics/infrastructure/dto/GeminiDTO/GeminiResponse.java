package br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO;

public record GeminiResponse(
        String caminhaoSugerido,
        String justificativa,
        String litrosGastos
) {
}