package br.com.senior.transport_logistics.domain.truck.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AverageDimensionsTrucks(
        double weightAvarege,
        double lengthAvarege,
        double heightAvarege
) {
}
