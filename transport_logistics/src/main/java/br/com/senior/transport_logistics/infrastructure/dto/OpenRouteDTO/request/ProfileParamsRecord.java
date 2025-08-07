package br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProfileParamsRecord(
        @JsonProperty("restrictions")
        RestrictionsRecord restrictions
) {
}
