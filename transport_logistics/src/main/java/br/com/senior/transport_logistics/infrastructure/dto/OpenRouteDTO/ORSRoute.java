package br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO;

import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.response.StepRecord;

import java.util.List;

public record ORSRoute(
        Double distance,
        double duration,
        List<StepRecord> steps
) {
}
