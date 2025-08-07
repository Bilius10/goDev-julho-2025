package br.com.senior.transport_logistics.dto.OpenRouteDTO.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OptionsRecord(
        @JsonProperty("profile_params")
        ProfileParamsRecord profileParams
) {
}
