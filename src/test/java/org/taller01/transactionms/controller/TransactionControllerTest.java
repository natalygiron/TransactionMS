package org.taller01.transactionms.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.taller01.transactionms.service.TransactionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@WebFluxTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TransactionService service;

    // ------- helpers without unused parameters (no inspection warnings) -------
    private Transaction baseTx(TransactionType type) {
        return Transaction.builder()
                .id("T1")
                .type(type)
                .status(TransactionStatus.SUCCESS)
                .fromAccountId("A1")
                .toAccountId("A2")
                .amount(new BigDecimal("100"))
                .message("ok")
                .createdAt(Instant.now())
                .build();
    }
    private Transaction txDeposit()   { return baseTx(TransactionType.DEPOSIT); }
    private Transaction txWithdrawal(){ return baseTx(TransactionType.WITHDRAWAL); }
    private Transaction txTransfer()  { return baseTx(TransactionType.TRANSFER); }

    // --------------------------
    // POST /transacciones/deposito
    // --------------------------
    @Nested @DisplayName("POST /transacciones/deposito")
    class Deposit {

        @Test
        @DisplayName("returns 200 with transaction payload")
        void deposit_success_200() {
            given(service.deposit(any(DepositRequest.class)))
                    .willReturn(Mono.just(txDeposit()));

            webTestClient.post()
                    .uri("/transacciones/deposito")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"accountId\":\"A1\",\"amount\":100}")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.id").isEqualTo("T1")
                    .jsonPath("$.type").isEqualTo("DEPOSIT")
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.amount").isEqualTo(100);
        }

        @Test
        @DisplayName("returns 400 when body is missing")
        void deposit_bad_request_400_when_body_empty() {
            webTestClient.post()
                    .uri("/transacciones/deposito")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    // ------------------------
    // POST /transacciones/retiro
    // ------------------------
    @Nested @DisplayName("POST /transacciones/retiro")
    class Withdraw {

        @Test
        @DisplayName("returns 200 with transaction payload")
        void withdraw_success_200() {
            given(service.withdraw(any(WithdrawRequest.class)))
                    .willReturn(Mono.just(txWithdrawal()));

            webTestClient.post()
                    .uri("/transacciones/retiro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"accountId\":\"A1\",\"amount\":50}")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.type").isEqualTo("WITHDRAWAL")
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.fromAccountId").isEqualTo("A1");
        }

        @Test
        @DisplayName("returns 400 when body is missing")
        void withdraw_bad_request_400_when_body_empty() {
            webTestClient.post()
                    .uri("/transacciones/retiro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    // --------------------------------
    // POST /transacciones/transferencia
    // --------------------------------
    @Nested @DisplayName("POST /transacciones/transferencia")
    class Transfer {

        @Test
        @DisplayName("returns 200 with transaction payload")
        void transfer_success_200() {
            given(service.transfer(any(TransferRequest.class)))
                    .willReturn(Mono.just(txTransfer()));

            webTestClient.post()
                    .uri("/transacciones/transferencia")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"fromAccountId\":\"A1\",\"toAccountId\":\"A2\",\"amount\":70}")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.type").isEqualTo("TRANSFER")
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.fromAccountId").isEqualTo("A1")
                    .jsonPath("$.toAccountId").isEqualTo("A2");
        }

        @Test
        @DisplayName("returns 400 when body is missing")
        void transfer_bad_request_400_when_body_empty() {
            webTestClient.post()
                    .uri("/transacciones/transferencia")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    // -----------------------------
    // GET /transacciones/historial
    // -----------------------------
    @Nested @DisplayName("GET /transacciones/historial")
    class History {

        @Test
        @DisplayName("returns 200 with list payload")
        void history_success_200() {
            TransactionResponse tr = TransactionResponse.from(txDeposit());
            given(service.getHistory(eq("A1"))).willReturn(Flux.just(tr));

            webTestClient.get()
                    .uri("/transacciones/historial?cuentaId=A1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$[0].id").isEqualTo("T1")
                    .jsonPath("$[0].type").isEqualTo("DEPOSIT")
                    .jsonPath("$[0].status").isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("returns 400 when cuentaId is missing")
        void history_bad_request_400_when_param_missing() {
            webTestClient.get()
                    .uri("/transacciones/historial")
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }
}
