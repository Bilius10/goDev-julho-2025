package br.com.senior.transport_logistics.domain.transport.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HubSummaryProjection(
        Long hubId,
        String hubName,
        String hubAddress,
        String[] trucks,
        String[] employees,
        BigDecimal totalFuelConsumption
) {}

