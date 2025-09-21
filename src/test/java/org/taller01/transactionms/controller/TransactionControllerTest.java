package org.taller01.transactionms.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionStatus;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.dto.response.TransactionResponse;
import org.taller01.transactionms.mapper.TransactionMapper;
import org.taller01.transactionms.service.TransactionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private TransactionService service;

    @MockBean
    private TransactionMapper mapper;

    private Transaction sampleTx(String id, TransactionType type, BigDecimal amount) {
        return Transaction.builder()
                .id(id)
                .type(type)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .createdAt(Instant.now())
                .build();
    }

    private TransactionResponse sampleResp(String id, TransactionType type, BigDecimal amount) {
        return TransactionResponse.builder()
                .id(id)
                .type(type)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .createdAt(Instant.now())
                .message("ok")
                .build();
    }

    @Test
    void deposit_returnsOk() {
        Transaction tx = sampleTx("1", TransactionType.DEPOSIT, BigDecimal.valueOf(100));
        TransactionResponse resp = sampleResp("1", TransactionType.DEPOSIT, BigDecimal.valueOf(100));

        when(service.deposit(any(DepositRequest.class))).thenReturn(Mono.just(tx));
        when(mapper.toResponse(tx)).thenReturn(resp);

        webClient.post()
                .uri("/transacciones/deposito")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DepositRequest("acc1", BigDecimal.valueOf(100)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionResponse.class)
                .isEqualTo(resp);
    }

    @Test
    void withdraw_returnsOk() {
        Transaction tx = sampleTx("2", TransactionType.WITHDRAWAL, BigDecimal.valueOf(50));
        TransactionResponse resp = sampleResp("2", TransactionType.WITHDRAWAL, BigDecimal.valueOf(50));

        when(service.withdraw(any(WithdrawRequest.class))).thenReturn(Mono.just(tx));
        when(mapper.toResponse(tx)).thenReturn(resp);

        webClient.post()
                .uri("/transacciones/retiro")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new WithdrawRequest("acc1", BigDecimal.valueOf(50)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionResponse.class)
                .isEqualTo(resp);
    }

    @Test
    void transfer_returnsOk() {
        Transaction tx = sampleTx("3", TransactionType.TRANSFER, BigDecimal.valueOf(200));
        TransactionResponse resp = sampleResp("3", TransactionType.TRANSFER, BigDecimal.valueOf(200));

        when(service.transfer(any(TransferRequest.class))).thenReturn(Mono.just(tx));
        when(mapper.toResponse(tx)).thenReturn(resp);

        webClient.post()
                .uri("/transacciones/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new TransferRequest("a1", "a2", BigDecimal.valueOf(200)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionResponse.class)
                .isEqualTo(resp);
    }

    @Test
    void history_returnsFlux() {
        Transaction tx1 = sampleTx("4", TransactionType.DEPOSIT, BigDecimal.valueOf(100));
        Transaction tx2 = sampleTx("5", TransactionType.WITHDRAWAL, BigDecimal.valueOf(30));
        TransactionResponse resp1 = sampleResp("4", TransactionType.DEPOSIT, BigDecimal.valueOf(100));
        TransactionResponse resp2 = sampleResp("5", TransactionType.WITHDRAWAL, BigDecimal.valueOf(30));

        when(service.getHistory("acc1")).thenReturn(Flux.just(tx1, tx2));
        when(mapper.toResponse(tx1)).thenReturn(resp1);
        when(mapper.toResponse(tx2)).thenReturn(resp2);

        webClient.get()
                .uri("/transacciones/historial?accountId=acc1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionResponse.class)
                .contains(resp1, resp2);
    }
}
