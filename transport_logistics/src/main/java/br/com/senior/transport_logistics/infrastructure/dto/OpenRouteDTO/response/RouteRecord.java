package br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.response;

import java.util.List;

public record RouteRecord(
        SummaryRecord summary,
        List<SegmentRecord> segments
) {
}
