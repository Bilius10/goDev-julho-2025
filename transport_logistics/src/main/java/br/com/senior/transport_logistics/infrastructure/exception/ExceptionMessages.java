package br.com.senior.transport_logistics.infrastructure.exception;

public enum ExceptionMessages {

    HUB_NOT_FOUND_BY_ID("Filial com o campo 'id' igual a '%s' não foi encontrado."),
    HUB_NAME_IN_USE("O campo nome com valor '%s' já está em uso para Filial."),
    HUB_CNPJ_IN_USE("O campo CNPJ com valor '%s' já está em uso para Filial."),
    HUB_ALREADY_EXISTS_IN_CITY("A cidade %s já possui uma filial.");

    private final String template;

    ExceptionMessages(String template) {
        this.template = template;
    }

    public String getMessage(Object... args) {
        return String.format(template, args);
    }
}

