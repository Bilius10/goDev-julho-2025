package br.com.senior.transport_logistics.domain.transport.enums;

import lombok.Getter;

@Getter
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

}