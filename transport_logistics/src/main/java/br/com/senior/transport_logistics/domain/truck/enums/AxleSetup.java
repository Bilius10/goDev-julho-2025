package br.com.senior.transport_logistics.domain.truck.enums;

public enum AxleSetup {
    AXLE_4x2("4x2"),
    AXLE_6x2("6x2"),
    AXLE_6x4("6x4");

    private final String description;

    AxleSetup(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
