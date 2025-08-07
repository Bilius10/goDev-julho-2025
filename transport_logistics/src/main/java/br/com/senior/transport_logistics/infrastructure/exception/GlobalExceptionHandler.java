package br.com.senior.transport_logistics.infrastructure.exception;

import br.com.senior.transport_logistics.infrastructure.exception.common.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        var response = buildResponse(ex.getStatus(), ex.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        var response = buildResponse(HttpStatus.BAD_REQUEST, "Dados inválidos", request.getRequestURI());

        ex.getBindingResult().getFieldErrors().forEach(f -> response.addError(f.getField(), f.getDefaultMessage()));

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        var response = buildResponse(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        var response = buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        var response = buildResponse(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception ex, HttpServletRequest request) {
        var response = buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.internalServerError().body(response);
    }

    private ExceptionResponse buildResponse(HttpStatus httpStatus, String message, String path) {
        return ExceptionResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}

