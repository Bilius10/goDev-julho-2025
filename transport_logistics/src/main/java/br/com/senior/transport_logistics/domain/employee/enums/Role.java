package br.com.senior.transport_logistics.domain.employee.enums;

public enum Role {
    DRIVER("Motorista"),
    ADMIN("Administrador"),
    MANAGER("Gerente");

    private String role;

    Role(String role) {
        this.role = role;
    }
}
