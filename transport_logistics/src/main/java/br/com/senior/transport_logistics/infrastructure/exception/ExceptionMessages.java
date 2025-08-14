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
    EMPLOYEE_PASSWORD_CONFIRMATION_MISMATCH("A nova senha e a confirmação não coincidem. Verifique os campos e tente novamente."),
    EMPLOYEE_WRONG_CURRENT_PASSWORD("A senha atual informada está incorreta. Por favor, revise e tente novamente."),
    DRIVER_NOT_FOUND("Nenhum motorista disponível foi encontrado para os critérios informados. Verifique os dados e tente novamente."),

    PRODUCT_NOT_FOUND_BY_ID("Produto com o campo 'id' igual a '%s' não foi encontrado"),
    PRODUCT_NAME_IN_USE("O campo nome com valor '%s' já está em uso por outro produto."),

    SHIPMENT_NOT_FOUND_BY_ID("Carga com o campo 'id' igual a '%s' não foi encontrado"),
    SHIPMENT_IS_NOT_PENDING("Carga com o status '%s'"),

    TRUCK_NOT_FOUND_BY_CODE("Caminhão com o campo 'code' igual a '%s' não foi encontrado"),
    TRUCK_NOT_FOUND_BY_ID("Caminhão com o campo 'id' igual a '%s' não foi encontrado"),
    TRUCK_NOT_SUPPORT_LOAD("Nenhum caminhão que suporta '%s'KG foi encontrado"),
    NO_TRUCK_IN_THE_SYSTEM("Nenhum caminhão cadastrado em sistemas"),

    TRANSPORT_NOT_FOUND_BY_ID("Transporte com o campos 'id' igual a '%s' não foi encontrado");

    private final String template;

    ExceptionMessages(String template) {
        this.template = template;
    }

    public String getMessage(Object... args) {
        return String.format(template, args);
    }
}

