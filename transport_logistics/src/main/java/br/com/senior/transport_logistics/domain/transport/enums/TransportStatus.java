package br.com.senior.transport_logistics.domain.transport.enums;

public enum TransportStatus {

    PENDING("Pendente"),
    ASSIGNED("Atribuída"),
    IN_TRANSIT("Em Trânsito"),
    DELIVERED("Entregue"),
    CANCELED("Cancelada"),
    RETURNED("Devolvida");

    private final String description;

    TransportStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}