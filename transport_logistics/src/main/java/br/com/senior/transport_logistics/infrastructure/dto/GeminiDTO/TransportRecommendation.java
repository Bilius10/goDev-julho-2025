package br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO;

public record TransportRecommendation(
        Long suggestedTruckId,
        Long returnShipmentId,
        String truckJustification,
        String returnShipmentJustification,
        Double litersSpentOutbound,
        Double litersSpentReturn
) {
}