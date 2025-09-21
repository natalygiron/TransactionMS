package org.taller01.transactionms.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebClientAccountAdapterTest {

    private ExchangeFunction exchangeFunction;
    private WebClientAccountAdapter adapter;

    @BeforeEach
    void setUp() {
        exchangeFunction = Mockito.mock(ExchangeFunction.class);

        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .exchangeFunction(exchangeFunction)
                .build();

        adapter = new WebClientAccountAdapter(client); // ✅ tu constructor solo recibe WebClient
    }

    @Test
    void getAccount_success() {
        String json = """
                {
                  "id": "a1",
                  "accountNumber": "123",
                  "balance": 200.0,
                  "type": "SAVINGS",
                  "clientId": "c1"
                }
                """;

        var bufferFactory = new DefaultDataBufferFactory();
        var dataBuffer = bufferFactory.wrap(json.getBytes());

        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.just(ClientResponse
                        .create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(Flux.just(dataBuffer))   // ✅ usar Flux
                        .build()));

        StepVerifier.create(adapter.getAccount("a1"))
                .expectNextMatches(acc ->
                        acc.id().equals("a1") &&
                                acc.accountNumber().equals("123") &&
                                acc.balance().compareTo(BigDecimal.valueOf(200.0)) == 0 &&
                                acc.clientId().equals("c1"))
                .verifyComplete();

        verify(exchangeFunction).exchange(any());
    }

    @Test
    void getAccount_notFound() {
        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.just(ClientResponse
                        .create(HttpStatus.NOT_FOUND)
                        .build()));

        StepVerifier.create(adapter.getAccount("unknown"))
                .expectErrorMatches(err -> err instanceof RuntimeException &&
                        err.getMessage().contains("404"))
                .verify();

        verify(exchangeFunction).exchange(any());
    }
}
