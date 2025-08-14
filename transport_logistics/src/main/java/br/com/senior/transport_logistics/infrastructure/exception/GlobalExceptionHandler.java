package br.com.senior.transport_logistics.infrastructure.exception;

import br.com.senior.transport_logistics.infrastructure.exception.common.BaseException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String INVALID_INPUT_DATA = "Dados de entrada inválidos";
    private static final String VALIDATION_CONSTRAINT_VIOLATION = "Violação de restrições de validação";
    private static final String INVALID_DATA_FORMAT = "Formato de dados inválido";
    private static final String MALFORMED_JSON = "JSON mal formado";
    private static final String JSON_MAPPING_ERROR = "Erro no mapeamento dos dados JSON";
    private static final String ACCESS_DENIED_MESSAGE = "Acesso negado para este recurso";
    private static final String DATA_INTEGRITY_VIOLATION = "Violação de integridade dos dados";
    private static final String DATA_ALREADY_EXISTS = "Dados já existem no sistema";
    private static final String INTERNAL_SERVER_ERROR = "Erro interno do servidor";

    private static final String METHOD_NOT_SUPPORTED_TEMPLATE = "Método '%s' não é suportado para este endpoint. Métodos permitidos: %s";
    private static final String PARAMETER_TYPE_MISMATCH_TEMPLATE = "Parâmetro '%s' deve ser do tipo %s";
    private static final String MISSING_PARAMETER_TEMPLATE = "Parâmetro obrigatório '%s' não foi fornecido";

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        if (ex.getStatus().is5xxServerError()) {
            log.error("Excecao base: {} - Caminho: {}", ex.getMessage(), request.getRequestURI(), ex);
        } else {
            log.warn("Excecao base: {} - Caminho: {} - Status: {}",
                    ex.getMessage(), request.getRequestURI(), ex.getStatus().value());
        }

        var response = buildResponse(
                ex.getStatus(),
                ex.getLocalizedMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        log.warn("Validacao falhou para requisicao: {} - Erros: {}",
                request.getRequestURI(),
                ex.getBindingResult().getFieldErrors().size());

        var response = buildResponse(
                HttpStatus.BAD_REQUEST,
                INVALID_INPUT_DATA,
                request.getRequestURI());

        ex.getBindingResult().getFieldErrors().forEach(f ->
                response.addError(
                        f.getField(), f.getDefaultMessage(), f.getRejectedValue()
                )
        );

        ex.getBindingResult().getGlobalErrors().forEach(globalError ->
                response.addError(
                        globalError.getObjectName(), globalError.getDefaultMessage(), null
                )
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Violacao de restricao para requisicao: {} - Violacoes: {}",
                request.getRequestURI(), ex.getConstraintViolations().size());

        var response = buildResponse(
                HttpStatus.BAD_REQUEST,
                VALIDATION_CONSTRAINT_VIOLATION,
                request.getRequestURI()
        );

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            response.addError(fieldName, violation.getMessage(), violation.getInvalidValue());
        });

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("Formato de corpo de requisicao invalido para: {} - Causa: {}",
                request.getRequestURI(), ex.getCause() != null ? ex.getCause().getClass().getSimpleName() : "Desconhecida");

        String message = INVALID_DATA_FORMAT;

        if (ex.getCause() instanceof JsonParseException) {
            message = MALFORMED_JSON;
        } else if (ex.getCause() instanceof JsonMappingException) {
            message = JSON_MAPPING_ERROR;
        }

        var response = buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        log.warn("Metodo HTTP nao suportado: {} para caminho: {} - Suportados: {}",
                request.getMethod(), request.getRequestURI(),
                ex.getSupportedMethods() != null ? String.join(", ", ex.getSupportedMethods()) : "Nenhum");

        String message = String.format(
                METHOD_NOT_SUPPORTED_TEMPLATE,
                request.getMethod(),
                String.join(", ", ex.getSupportedMethods())
        );

        var response = buildResponse(HttpStatus.METHOD_NOT_ALLOWED, message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        log.warn("Incompatibilidade de tipo de parametro para: {} - Parametro: '{}', Esperado: {}, Atual: '{}'",
                request.getRequestURI(), ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Desconhecido",
                ex.getValue());

        String message = String.format(
                PARAMETER_TYPE_MISMATCH_TEMPLATE,
                ex.getName(),
                ex.getRequiredType().getSimpleName()
        );

        var response = buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        response.addError(ex.getName(), message, ex.getValue());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        log.warn("Parametro obrigatorio ausente: '{}' para caminho: {}",
                ex.getParameterName(), request.getRequestURI());

        String message = String.format(MISSING_PARAMETER_TEMPLATE, ex.getParameterName());
        var response = buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        response.addError(ex.getParameterName(), message, null);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Acesso negado para caminho: {} - Usuario: {} - Motivo: {}",
                request.getRequestURI(),
                getCurrentUsername(),
                ex.getMessage());

        var response = buildResponse(
                HttpStatus.FORBIDDEN,
                ACCESS_DENIED_MESSAGE,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Violacao de integridade de dados para caminho: {} - Causa: {}",
                request.getRequestURI(),
                ex.getCause() != null ? ex.getCause().getClass().getSimpleName() : "Desconhecida");

        String message = DATA_INTEGRITY_VIOLATION;

        if (ex.getCause() instanceof ConstraintViolationException) {
            message = DATA_ALREADY_EXISTS;
        }

        var response = buildResponse(HttpStatus.CONFLICT, message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Erro inesperado para caminho: {} - Excecao: {}",
                request.getRequestURI(), ex.getClass().getSimpleName(), ex);

        var response = buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );

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

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonimo";
    }
}