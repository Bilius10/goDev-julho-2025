package br.com.senior.transport_logistics.infrastructure.exception;

public enum ExceptionMessages {

    HUB_NOT_FOUND_BY_ID("Filial com o campo 'id' igual a '%s' não foi encontrado."),
    HUB_NAME_IN_USE("O campo nome com valor '%s' já está em uso por outra filial."),
    HUB_CNPJ_IN_USE("O campo CNPJ com valor '%s' já está em uso por outra filial."),
    HUB_ALREADY_EXISTS_IN_CITY("A cidade %s já possui uma filial."),

    EMPLOYEE_NOT_FOUND_BY_ID("Funcionário com o campo 'id' igual a '%s' não foi encontrado"),
    EMPLOYEE_NOT_FOUND_BY_EMAIL("Funcionário com o campo 'email' igual a '%s' não foi encontrado"),
    EMPLOYEE_EMAIL_IN_USE("O campo email com valor '%s' já está em uso por outro funcionário"),
    EMPLOYEE_CPF_IN_USE("O campo CPF com valor '%s' já está em uso por outro funcionário."),
    EMPLOYEE_CNH_IN_USE("O campo CNH com valor '%s' já está em uso por outro funcionário."),

    PRODUCT_NOT_FOUND_BY_ID("Produto com o campo 'id' igual a '%s' não foi encontrado"),
    PRODUCT_NAME_IN_USE("O campo nome com valor '%s' já está em uso por outro produto."),

    SHIPMENT_NOT_FOUND_BY_ID("Carga com o campo 'id' igual a '%s' não foi encontrado"),

    TRUCK_NOT_FOUND_BY_CODE("Caminhão com o campo 'id' igual a '%s' não foi encontrado");

    private final String template;

    ExceptionMessages(String template) {
        this.template = template;
    }

    public String getMessage(Object... args) {
        return String.format(template, args);
    }
}

