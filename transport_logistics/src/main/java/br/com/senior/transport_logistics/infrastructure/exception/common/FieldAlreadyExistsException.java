package br.com.senior.transport_logistics.infrastructure.exception.common;

import org.springframework.http.HttpStatus;

public class FieldAlreadyExistsException extends BaseException {
    public FieldAlreadyExistsException(String entity, String field, String value) {
        super(String.format("O campo %s com valor '%s' já está em uso para %s.", field, value, entity));
    }


    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
