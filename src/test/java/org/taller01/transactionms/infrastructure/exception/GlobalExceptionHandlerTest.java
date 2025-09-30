package org.taller01.transactionms.infrastructure.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.taller01.transactionms.domain.exception.ResourceNotFoundException;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        exchange = mock(ServerWebExchange.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(exchange.getRequest().getPath().value()).thenReturn("/transacciones/test");
    }

    @Test
    void notFound_shouldReturn404() {
        var ex = new ResourceNotFoundException("no encontrado");
        ResponseEntity<ApiError> response = handler.notFound(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("no encontrado");
    }

    @Test
    void status_shouldReturnCustomStatus() {
        var ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "error de estado");
        ResponseEntity<ApiError> response = handler.status(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("error de estado");
    }

    @Test
    void beanValidation_shouldReturn400WithFieldErrors() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "field1", "must not be null"));
        var ex = new WebExchangeBindException(null, bindingResult);

        ResponseEntity<ApiError> response = handler.beanValidation(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getFieldErrors()).containsKey("field1");
    }

    @Test
    void badInput_shouldReturn400() {
        var ex = new ServerWebInputException("invalid input");
        ResponseEntity<ApiError> response = handler.badInput(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("JSON/params inválidos");
    }

    @Test
    void downstream_shouldReturnErrorFromAccountMS() {
        var ex = WebClientResponseException.create(
                404, "Not Found", null,
                "error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        ResponseEntity<ApiError> response = handler.downstream(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("AccountMS");
    }

    @Test
    void clientTimeout_shouldReturnGatewayTimeout() {
        var ex = new TimeoutException("timeout");
        ResponseEntity<ApiError> response = handler.clientTimeout(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody().getMessage()).contains("Timeout");
    }

    @Test
    void clientConnect_shouldReturnServiceUnavailable() {
        var ex = new ConnectException("no connection");
        ResponseEntity<ApiError> response = handler.clientConnect(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getMessage()).contains("No se pudo conectar");
    }

    @Test
    void duplicate_shouldReturnConflict() {
        var ex = new DuplicateKeyException("duplicate");
        ResponseEntity<ApiError> response = handler.duplicate(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("Conflicto");
    }

    @Test
    void unexpected_shouldReturn500() {
        var ex = new RuntimeException("boom");
        ResponseEntity<ApiError> response = handler.unexpected(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("inesperado");
    }
}
