package br.com.senior.transport_logistics.infrastructure.exception.common;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {
    protected BaseException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatus();
}
