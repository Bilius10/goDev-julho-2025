package br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO;

public record GeminiResponse(
        Long caminhaoSugerido,
        String justificativa,
        Double litrosGastos
) {
}