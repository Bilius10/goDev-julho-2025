package br.com.senior.transport_logistics.infrastructure.exception.common;

import org.springframework.http.HttpStatus;

public class TransportException extends BaseException{
    public TransportException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
