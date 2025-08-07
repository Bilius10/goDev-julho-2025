package br.com.senior.transport_logistics.domain.truck.enums;

public enum TruckBody {
    FLATBED("Prancha"),
    DRY_VAN("Baú Seca"),
    REEFER_VAN("Baú Frigorífico"),
    DUMP_BODY("Caçamba"),
    TANKER("Tanque"),
    CURTAINSIDE("Sider"),
    CAR_HAULER("Transporte de Veículos"),
    LIVESTOCK_TRAILER("Reboque para Animais");

    private final String description;

    TruckBody(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
