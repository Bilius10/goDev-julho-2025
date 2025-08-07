package br.com.senior.transport_logistics.dto.OpenRouteDTO.response;

import java.util.List;

public record OrsResponse(
        List<RouteRecord> routes
) {
}
