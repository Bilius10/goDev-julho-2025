package br.com.senior.transport_logistics.infrastructure.exception.common;

import org.springframework.http.HttpStatus;

public class JWTException extends BaseException {
    public JWTException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}

