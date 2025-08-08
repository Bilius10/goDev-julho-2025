package br.com.senior.transport_logistics.infrastructure.exception.common;

import org.springframework.http.HttpStatus;

public class FieldAlreadyExistsException extends BaseException {
    public FieldAlreadyExistsException(String message) {
        super(message);
    }


    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
