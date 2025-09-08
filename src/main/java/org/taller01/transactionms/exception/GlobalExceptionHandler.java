package org.taller01.transactionms.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.net.ConnectException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           ServerWebExchange exchange, Map<String, String> fields) {
        String path = exchange.getRequest().getPath().value();
        String error = status.is4xxClientError() ? "Solicitud incorrecta" : "Error del servidor";
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .fieldErrors(fields)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, ServerWebExchange exchange) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), exchange, null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> status(ResponseStatusException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(status, ex.getReason(), exchange, null);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiError> beanValidation(WebExchangeBindException ex, ServerWebExchange exchange) {
        Map<String, String> fields = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Hay errores de validación en el cuerpo enviado", exchange, fields);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiError> badInput(ServerWebInputException ex, ServerWebExchange exchange) {
        return build(HttpStatus.BAD_REQUEST, "JSON/params inválidos o con formato incorrecto", exchange, null);
    }

    // ⇣ Errores hacia AccountMS vía WebClient
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> downstream(WebClientResponseException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String msg = status.is4xxClientError()
                ? "Error al aplicar operación en AccountMS"
                : "AccountMS no disponible";
        return build(status, msg, exchange, null);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiError> clientTimeout(TimeoutException ex, ServerWebExchange exchange) {
        return build(HttpStatus.GATEWAY_TIMEOUT, "Timeout llamando a AccountMS", exchange, null);
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<ApiError> clientConnect(ConnectException ex, ServerWebExchange exchange) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo conectar a AccountMS", exchange, null);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> duplicate(DuplicateKeyException ex, ServerWebExchange exchange) {
        return build(HttpStatus.CONFLICT, "Conflicto de clave duplicada", exchange, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error at {}: {}", exchange.getRequest().getPath(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", exchange, null);
    }
}
