package br.com.senior.transport_logistics.domain.shipment.dto.response;

public record ShipmentResponseDTO(
        Long id,
        Double weight,
        Integer quantity,
        String notes,
        String productName,
        boolean isHazardous
) {

    public ShipmentResponseDTO(Long id, Double weight, Integer quantity, String notes, String productName, boolean isHazardous) {
        this.id = id;
        this.weight = weight;
        this.quantity = quantity;
        this.notes = notes;
        this.productName = productName;
        this.isHazardous = isHazardous;
    }
}
