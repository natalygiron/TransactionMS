package org.taller01.transactionms.infrastructure.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.model.TransactionStatus;
import org.taller01.transactionms.domain.model.TransactionType;
import org.taller01.transactionms.domain.port.in.TransactionUseCase;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.dto.response.TransactionResponse;
import org.taller01.transactionms.infrastructure.mapper.TransactionMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@WebFluxTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @MockBean
    private TransactionUseCase service;

    @MockBean
    private TransactionMapper mapper;

    @Autowired
    private WebTestClient client;

    @Test
    void deposit_shouldReturnTransactionResponse() {
        var request = new DepositRequest("acc1", BigDecimal.TEN);
        var tx = Transaction.builder()
                .id("id1").type(TransactionType.DEPOSIT).status(TransactionStatus.SUCCESS)
                .amount(BigDecimal.TEN).createdAt(Instant.now()).build();

        var response = new TransactionResponse("id1", TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS, null, "acc1", BigDecimal.TEN, Instant.now(), "ok");

        Mockito.when(service.deposit(request)).thenReturn(Mono.just(tx));
        Mockito.when(mapper.toResponse(tx)).thenReturn(response);

        client.post().uri("/transacciones/deposito")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("id1")
                .jsonPath("$.type").isEqualTo("DEPOSIT");
    }

    @Test
    void withdraw_shouldReturnTransactionResponse() {
        var request = new WithdrawRequest("acc1", BigDecimal.ONE);
        var tx = Transaction.builder()
                .id("id2").type(TransactionType.WITHDRAWAL).status(TransactionStatus.SUCCESS)
                .amount(BigDecimal.ONE).createdAt(Instant.now()).build();

        var response = new TransactionResponse("id2", TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS, "acc1", null, BigDecimal.ONE, Instant.now(), "ok");

        Mockito.when(service.withdraw(request)).thenReturn(Mono.just(tx));
        Mockito.when(mapper.toResponse(tx)).thenReturn(response);

        client.post().uri("/transacciones/retiro")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("id2")
                .jsonPath("$.type").isEqualTo("WITHDRAWAL");
    }

    @Test
    void transfer_shouldReturnTransactionResponse() {
        var request = new TransferRequest("acc1", "acc2", BigDecimal.valueOf(50));
        var tx = Transaction.builder()
                .id("id3").type(TransactionType.TRANSFER).status(TransactionStatus.SUCCESS)
                .fromAccountId("acc1").toAccountId("acc2")
                .amount(BigDecimal.valueOf(50)).createdAt(Instant.now()).build();

        var response = new TransactionResponse("id3", TransactionType.TRANSFER,
                TransactionStatus.SUCCESS, "acc1", "acc2", BigDecimal.valueOf(50),
                Instant.now(), "ok");

        Mockito.when(service.transfer(request)).thenReturn(Mono.just(tx));
        Mockito.when(mapper.toResponse(tx)).thenReturn(response);

        client.post().uri("/transacciones/transferencia")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("id3")
                .jsonPath("$.type").isEqualTo("TRANSFER");
    }

    @Test
    void history_shouldReturnTransactionList() {
        var tx = Transaction.builder()
                .id("id4").type(TransactionType.DEPOSIT).status(TransactionStatus.SUCCESS)
                .toAccountId("acc1").amount(BigDecimal.TEN).createdAt(Instant.now()).build();

        var response = new TransactionResponse("id4", TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS, null, "acc1", BigDecimal.TEN, Instant.now(), "ok");

        Mockito.when(service.getHistory("acc1")).thenReturn(Flux.just(tx));
        Mockito.when(mapper.toResponse(tx)).thenReturn(response);

        client.get().uri("/transacciones/historial?accountId=acc1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("id4")
                .jsonPath("$[0].type").isEqualTo("DEPOSIT");
    }
}
