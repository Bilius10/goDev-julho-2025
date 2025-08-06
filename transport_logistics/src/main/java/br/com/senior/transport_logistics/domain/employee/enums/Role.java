package br.com.senior.transport_logistics.domain.employee.enums;

public enum Role {
    BOSS("boos"),
    DRIVER("driver");

    private String role;

    Role(String role) {
        this.role = role;
    }
}
