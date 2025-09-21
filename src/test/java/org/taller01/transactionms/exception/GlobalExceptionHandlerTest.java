package org.taller01.transactionms.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.net.ConnectException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        // Mockear un request válido
        ServerHttpRequest request = MockServerHttpRequest.get("/test-path").build();
        exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
    }

    @Test
    void handleResourceNotFound() {
        ResponseEntity<ApiError> response =
                handler.notFound(new ResourceNotFoundException("No encontrado"), exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("No encontrado");
    }

    @Test
    void handleResponseStatusException() {
        ResponseEntity<ApiError> response =
                handler.status(new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad request"), exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("bad request");
    }

    @Test
    void handleBeanValidation() {
        // Simular un error de validación
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        when(ex.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiError> response = handler.beanValidation(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        Map<String, String> fields = response.getBody().getFieldErrors();
        assertThat(fields).containsEntry("field", "must not be null");
    }

    @Test
    void handleBadInput() {
        ResponseEntity<ApiError> response =
                handler.badInput(new ServerWebInputException("bad input"), exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("JSON/params inválidos");
    }

    @Test
    void handleDownstreamError() {
        WebClientResponseException ex = WebClientResponseException.create(
                500, "Server error", null, null, null);

        ResponseEntity<ApiError> response = handler.downstream(ex, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("AccountMS");
    }

    @Test
    void handleClientTimeout() {
        ResponseEntity<ApiError> response =
                handler.clientTimeout(new TimeoutException("timeout"), exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Timeout");
    }

    @Test
    void handleClientConnect() {
        ResponseEntity<ApiError> response =
                handler.clientConnect(new ConnectException("connection refused"), exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("AccountMS");
    }


    @Test
    void handleDuplicateKey() {
        ResponseEntity<ApiError> response =
                handler.duplicate(new DuplicateKeyException("duplicate"), exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("duplicada");
    }

    @Test
    void handleUnexpected() {
        ResponseEntity<ApiError> response =
                handler.unexpected(new RuntimeException("unexpected"), exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("error inesperado");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(Instant.now());
    }
}
