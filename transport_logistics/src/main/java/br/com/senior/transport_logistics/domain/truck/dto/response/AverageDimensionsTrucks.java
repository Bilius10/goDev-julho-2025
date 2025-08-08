package br.com.senior.transport_logistics.domain.truck.dto.response;

public record AverageDimensionsTrucks(
        double weightAvarege,
        double lengthAvarege,
        double heightAvarege
) {
}
