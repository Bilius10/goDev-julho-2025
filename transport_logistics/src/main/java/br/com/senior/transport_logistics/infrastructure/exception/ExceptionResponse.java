package br.com.senior.transport_logistics.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
public class ExceptionResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> details;

    public void addError(String field, String message) {
        if (this.details == null) {
            this.details = new ArrayList<>();
        }

        this.details.add(new FieldError(field, message));
    }

    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }
}
