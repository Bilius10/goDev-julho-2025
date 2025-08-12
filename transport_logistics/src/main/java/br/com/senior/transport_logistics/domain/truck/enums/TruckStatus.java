package br.com.senior.transport_logistics.domain.truck.enums;

public enum TruckStatus {
    AVAILABLE("Disponível"),
    IN_TRANSIT("Em Trânsito"),
    MAINTENANCE("Manutenção"),
    OUT_OF_ORDER("Fora de Serviço");

    private final String description;

    TruckStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
