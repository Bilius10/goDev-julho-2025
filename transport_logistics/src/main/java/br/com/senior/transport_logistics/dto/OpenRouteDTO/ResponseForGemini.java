package br.com.senior.transport_logistics.dto.OpenRouteDTO;

import br.com.senior.transport_logistics.dto.OpenRouteDTO.response.RouteRecord;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.response.SegmentRecord;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.response.StepRecord;

import java.util.List;

public record ResponseForGemini(
        Double distance,
        List<StepRecord> steps
) {
}
