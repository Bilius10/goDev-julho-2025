package br.com.senior.transport_logistics.domain.truck.enums;

public enum TruckType {
    PICKUP_TRUCK("Picape"),
    VAN("Van"),
    LIGHT_DUTY_TRUCK("Caminhão Leve"),
    MEDIUM_DUTY_TRUCK("Caminhão Médio"),
    HEAVY_DUTY_TRUCK("Caminhão Pesado"),
    SEMI_TRUCK("Cavalo Mecânico");

    private final String description;

    TruckType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
