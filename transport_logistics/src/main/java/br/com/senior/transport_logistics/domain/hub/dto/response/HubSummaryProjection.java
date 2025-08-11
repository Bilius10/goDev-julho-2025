package br.com.senior.transport_logistics.domain.hub.dto.response;

import lombok.Builder;

import java.util.List;

public record HubSummaryProjection(
        Long hubId,
        String hubName,
        String hubAddress,
        List<String> trucks,
        List<String> employees,
        Double totalFuelConsumption
) {
}
