package org.taller01.transactionms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionStatus;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.repository.TransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

    private TransactionRepository repo;   // mock
    private WebClient webClient;          // real con exchange stub
    private TransactionService service;

    private DepositRequest depositReq;
    private WithdrawRequest withdrawReq;
    private TransferRequest transferReq;

    @BeforeEach
    void setUp() {
        repo = mock(TransactionRepository.class);
        depositReq  = new DepositRequest("A1", new BigDecimal("100.00"));
        withdrawReq = new WithdrawRequest("A1", new BigDecimal("50.00"));
        transferReq = new TransferRequest("A1", "A2", new BigDecimal("70.00"));
    }

    /* -------------------- WebClients stub (sin parámetros constantes) -------------------- */

    /** POST (depósito/retiro) -> 200 OK */
    private WebClient wcDepositWithdrawOk() {
        ExchangeFunction fx = (ClientRequest req) -> {
            if (req.method() == HttpMethod.POST) {
                return Mono.just(ClientResponse.create(HttpStatus.OK).build());
            }
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        };
        return WebClient.builder().exchangeFunction(fx).build();
    }

    /** POST (depósito/retiro) -> 409 CONFLICT (falla) */
    private WebClient wcDepositWithdrawConflict() {
        ExchangeFunction fx = (ClientRequest req) -> {
            if (req.method() == HttpMethod.POST) {
                return Mono.just(ClientResponse.create(HttpStatus.CONFLICT).build());
            }
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        };
        return WebClient.builder().exchangeFunction(fx).build();
    }

    /** Transferencia: GET /cuentas/{id} devuelve balance; POSTs -> 200 OK. */
    private WebClient wcTransferOk(BigDecimal fromBalance) {
        ExchangeFunction fx = (ClientRequest req) -> {
            URI u = req.url();
            if (req.method() == HttpMethod.GET && u.getPath().startsWith("/cuentas/")) {
                boolean isFrom = u.getPath().endsWith("/" + transferReq.fromAccountId());
                String json = "{\"balance\":" + (isFrom ? fromBalance : "100000") + "}";
                return Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body(json)
                                .build()
                );
            }
            if (req.method() == HttpMethod.POST && (u.getPath().contains("/retiro") || u.getPath().contains("/deposito"))) {
                return Mono.just(ClientResponse.create(HttpStatus.OK).build());
            }
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        };
        return WebClient.builder().exchangeFunction(fx).build();
    }

    /* ---------------------------------------- TESTS ---------------------------------------- */

    @Nested @DisplayName("deposit()")
    class Deposit {
        @Test @DisplayName("SUCCESS cuando AccountMS responde 200")
        void deposit_success() {
            webClient = wcDepositWithdrawOk();
            service = new TransactionService(repo, webClient);

            when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.deposit(depositReq))
                    .assertNext(tx -> {
                        assert tx.getType() == TransactionType.DEPOSIT;
                        assert tx.getStatus() == TransactionStatus.SUCCESS;
                        assert "A1".equals(tx.getToAccountId());
                        assert tx.getAmount().compareTo(new BigDecimal("100.00")) == 0;
                        assert tx.getCreatedAt() != null;
                    })
                    .verifyComplete();
        }

        @Test @DisplayName("FAILED cuando AccountMS responde 409/5xx")
        void deposit_failed() {
            webClient = wcDepositWithdrawConflict();
            service = new TransactionService(repo, webClient);

            when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.deposit(depositReq))
                    .assertNext(tx -> {
                        assert tx.getType() == TransactionType.DEPOSIT;
                        assert tx.getStatus() == TransactionStatus.FAILED;
                        assert tx.getMessage() != null && !tx.getMessage().isBlank();
                    })
                    .verifyComplete();
        }
    }

    @Nested @DisplayName("withdraw()")
    class Withdraw {
        @Test @DisplayName("SUCCESS cuando AccountMS responde 200")
        void withdraw_success() {
            webClient = wcDepositWithdrawOk();
            service = new TransactionService(repo, webClient);

            when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.withdraw(withdrawReq))
                    .assertNext(tx -> {
                        assert tx.getType() == TransactionType.WITHDRAWAL;
                        assert tx.getStatus() == TransactionStatus.SUCCESS;
                        assert "A1".equals(tx.getFromAccountId());
                    })
                    .verifyComplete();
        }

        @Test @DisplayName("FAILED cuando AccountMS responde 409/5xx")
        void withdraw_failed() {
            webClient = wcDepositWithdrawConflict();
            service = new TransactionService(repo, webClient);

            when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.withdraw(withdrawReq))
                    .assertNext(tx -> {
                        assert tx.getType() == TransactionType.WITHDRAWAL;
                        assert tx.getStatus() == TransactionStatus.FAILED;
                        assert tx.getMessage() != null && !tx.getMessage().isBlank();
                    })
                    .verifyComplete();
        }
    }

    @Nested @DisplayName("transfer()")
    class Transfer {
        @Test @DisplayName("SUCCESS con saldo suficiente y POSTs 200")
        void transfer_success() {
            webClient = wcTransferOk(new BigDecimal("1000.00"));
            service = new TransactionService(repo, webClient);

            when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.transfer(transferReq))
                    .assertNext(tx -> {
                        assert tx.getType() == TransactionType.TRANSFER;
                        assert tx.getStatus() == TransactionStatus.SUCCESS;
                        assert "A1".equals(tx.getFromAccountId());
                        assert "A2".equals(tx.getToAccountId());
                    })
                    .verifyComplete();
        }

        @Test @DisplayName("FAILED cuando fromAccount == toAccount")
        void transfer_same_account_failed() {
            service = new TransactionService(repo, wcDepositWithdrawOk());
            TransferRequest bad = new TransferRequest("A1", "A1", new BigDecimal("10"));

            when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.transfer(bad))
                    .assertNext(tx -> {
                        assert tx.getType() == TransactionType.TRANSFER;
                        assert tx.getStatus() == TransactionStatus.FAILED;
                        assert tx.getMessage().toLowerCase().contains("misma cuenta");
                    })
                    .verifyComplete();
        }

        @Test @DisplayName("FAILED cuando saldo insuficiente en cuenta origen")
        void transfer_insufficient_balance_failed() {
            // GET devolverá balance bajo; el flujo falla antes de intentar POSTs
            webClient = wcTransferOk(new BigDecimal("20.00"));
            service = new TransactionService(repo, webClient);

            when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.transfer(transferReq))
                    .assertNext(tx -> {
                        assert tx.getType() == TransactionType.TRANSFER;
                        assert tx.getStatus() == TransactionStatus.FAILED;
                        assert tx.getMessage().toLowerCase().contains("saldo insuficiente");
                    })
                    .verifyComplete();
        }
    }

    @Nested @DisplayName("getHistory()")
    class History {
        @Test @DisplayName("mapea repo -> TransactionResponse")
        void history_maps_to_response() {
            Transaction sample = Transaction.builder()
                    .id("T1")
                    .type(TransactionType.DEPOSIT)
                    .status(TransactionStatus.SUCCESS)
                    .fromAccountId(null)
                    .toAccountId("A1")
                    .amount(new BigDecimal("10"))
                    .createdAt(Instant.now())
                    .message("ok")
                    .build();

            when(repo.findByFromAccountIdOrToAccountId("A1", "A1"))
                    .thenReturn(Flux.just(sample));

            service = new TransactionService(repo, wcDepositWithdrawOk());

            StepVerifier.create(service.getHistory("A1"))
                    .assertNext(resp -> {
                        assert resp != null;
                        assert "T1".equals(resp.getId());
                    })
                    .verifyComplete();
        }
    }
}
