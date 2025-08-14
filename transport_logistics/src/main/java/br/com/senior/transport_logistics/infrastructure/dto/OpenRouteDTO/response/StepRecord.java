package br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record StepRecord(
        double distance,
        double duration,
        int type,
        String instruction,
        String name,
        @JsonProperty("way_points")
        List<Integer> wayPoints,
        Integer exit_number
) {
}
