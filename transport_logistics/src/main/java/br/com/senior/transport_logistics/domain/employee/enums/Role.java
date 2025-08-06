package br.com.senior.transport_logistics.domain.employee.enums;

public enum Role {
    BOSS("Chefe"),
    DRIVER("Motorista");

    private String role;

    Role(String role) {
        this.role = role;
    }
}
