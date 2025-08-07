package br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO;

import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.response.StepRecord;

import java.util.List;

public record ResponseForGemini(
        Double distance,
        List<StepRecord> steps
) {
}
