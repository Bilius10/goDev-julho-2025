package br.com.senior.transport_logistics.infrastructure.exception.external;

import br.com.senior.transport_logistics.infrastructure.exception.common.BaseException;
import org.springframework.http.HttpStatus;

public class ErrorForRequest extends BaseException {
    public ErrorForRequest(String message) {
        super(message);
    }


    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
