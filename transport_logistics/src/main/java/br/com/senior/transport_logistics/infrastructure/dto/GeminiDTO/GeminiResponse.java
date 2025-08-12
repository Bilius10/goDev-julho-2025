package br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO;

public record GeminiResponse(
        Long caminhaoSugerido,
        Long produtoSelecionadoRetorno,
        String justificativaCaminhao,
        String justificativaCargaRetorno,
        Double litrosGastosIda,
        Double litrosGastosVolta
) {
}