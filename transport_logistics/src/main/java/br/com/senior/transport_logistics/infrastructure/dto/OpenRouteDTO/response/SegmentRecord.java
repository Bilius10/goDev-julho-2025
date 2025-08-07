package br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.response;

import java.util.List;

public record SegmentRecord(
        double distance,
        double duration,
        List<StepRecord> steps
) {
}
