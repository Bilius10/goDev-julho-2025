package br.com.senior.transport_logistics.dto.OpenRouteDTO.request;

import br.com.senior.transport_logistics.dto.NominationDTO.CoordinatesDTO;

import java.util.List;

public record OpenRouteRequestBody(
        List<List<CoordinatesDTO>> coordinates,
        String preference,
        OptionsRecord options
) {
}
