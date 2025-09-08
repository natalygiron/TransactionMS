package org.taller01.transactionms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionStatus;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.repository.TransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TransactionService {

    private final TransactionRepository repo;
    private final WebClient webClient;

    public TransactionService(TransactionRepository repo,
                              WebClient.Builder builder,
                              @Value("${accountms.base-url}") String accountBaseUrl) {
        this.repo = repo;
        this.webClient = builder.baseUrl(accountBaseUrl).build();
    }

    // --------------------------------------------------------------------------------
    // Operaciones públicas
    // --------------------------------------------------------------------------------

    public Mono<Transaction> deposit(DepositRequest req) {
        return callAccountDeposit(req.accountId(), req.amount())
                .then(save(Transaction.builder()
                        .type(TransactionType.DEPOSIT)
                        .status(TransactionStatus.SUCCESS)
                        .fromAccountId(null)
                        .toAccountId(req.accountId())
                        .amount(req.amount())
                        .createdAt(Instant.now())
                        .message("Depósito aplicado")
                        .build()))
                .onErrorResume(e -> saveFailed(TransactionType.DEPOSIT, null, req.accountId(), req.amount(), e));
    }

    public Mono<Transaction> withdraw(WithdrawRequest req) {
        return callAccountWithdraw(req.accountId(), req.amount())
                .then(save(Transaction.builder()
                        .type(TransactionType.WITHDRAWAL)
                        .status(TransactionStatus.SUCCESS)
                        .fromAccountId(req.accountId())
                        .toAccountId(null)
                        .amount(req.amount())
                        .createdAt(Instant.now())
                        .message("Retiro aplicado")
                        .build()))
                .onErrorResume(e -> saveFailed(TransactionType.WITHDRAWAL, req.accountId(), null, req.amount(), e));
    }

    public Mono<Transaction> transfer(TransferRequest req) {
        if (req.fromAccountId().equals(req.toAccountId())) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cuenta origen y destino no pueden ser iguales"));
        }

        // 1) Intentar retirar del origen
        return callAccountWithdraw(req.fromAccountId(), req.amount())

                // 2) Solo si el retiro fue OK, intentar depositar al destino
                .then(Mono.defer(() ->
                        callAccountDeposit(req.toAccountId(), req.amount())

                                // 3) Si el depósito fue OK, guardar SUCCESS
                                .then(save(Transaction.builder()
                                        .type(TransactionType.TRANSFER)
                                        .status(TransactionStatus.SUCCESS)
                                        .fromAccountId(req.fromAccountId())
                                        .toAccountId(req.toAccountId())
                                        .amount(req.amount())
                                        .createdAt(Instant.now())
                                        .message("Transferencia aplicada")
                                        .build()))

                                // 4) Si el depósito falla, COMPENSAR el retiro y guardar FAILED
                                .onErrorResume(e ->
                                        callAccountDeposit(req.fromAccountId(), req.amount())
                                                .onErrorResume(comp -> Mono.empty()) // si la compensación falla, igual registramos FAILED
                                                .then(saveFailed(TransactionType.TRANSFER,
                                                        req.fromAccountId(), req.toAccountId(), req.amount(), e)))
                ))

                // 5) Si el retiro falla, NO compensar (porque no se debitó). Guardar FAILED.
                .onErrorResume(e ->
                        saveFailed(TransactionType.TRANSFER,
                                req.fromAccountId(), req.toAccountId(), req.amount(), e));
    }


    public Flux<Transaction> history(String accountId) {
        // historial por cuenta (origen o destino)
        return repo.findByFromAccountIdOrToAccountId(accountId, accountId);
    }

    // --------------------------------------------------------------------------------
    // Helpers privados
    // --------------------------------------------------------------------------------

    private Mono<Void> callAccountDeposit(String accountId, BigDecimal amount) {
        return webClient.post()
                .uri(uri -> uri.path("/cuentas/internal/{id}/deposito")
                        .queryParam("amount", amount)
                        .build(accountId))
                .retrieve()
                .onStatus(code -> code.value() == 409, r ->
                        r.bodyToMono(String.class).defaultIfEmpty("Conflicto").map(msg ->
                                new ResponseStatusException(HttpStatus.CONFLICT, msg)))
                .onStatus(HttpStatusCode::is4xxClientError, r ->
                        r.bodyToMono(String.class).defaultIfEmpty("Error 4xx en AccountMS").map(msg ->
                                new ResponseStatusException(HttpStatus.BAD_REQUEST, msg)))
                .onStatus(HttpStatusCode::is5xxServerError, r ->
                        r.bodyToMono(String.class).defaultIfEmpty("AccountMS no disponible").map(msg ->
                                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, msg)))
                .toBodilessEntity()
                .then();
    }

    private Mono<Void> callAccountWithdraw(String accountId, BigDecimal amount) {
        return webClient.post()
                .uri(uri -> uri.path("/cuentas/internal/{id}/retiro")
                        .queryParam("amount", amount)
                        .build(accountId))
                .retrieve()
                .onStatus(code -> code.value() == 409, r ->
                        r.bodyToMono(String.class).defaultIfEmpty("Saldo insuficiente").map(msg ->
                                new ResponseStatusException(HttpStatus.CONFLICT, msg)))
                .onStatus(HttpStatusCode::is4xxClientError, r ->
                        r.bodyToMono(String.class).defaultIfEmpty("Error 4xx en AccountMS").map(msg ->
                                new ResponseStatusException(HttpStatus.BAD_REQUEST, msg)))
                .onStatus(HttpStatusCode::is5xxServerError, r ->
                        r.bodyToMono(String.class).defaultIfEmpty("AccountMS no disponible").map(msg ->
                                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, msg)))
                .toBodilessEntity()
                .then();
    }

    private Mono<Transaction> save(Transaction t) {
        return repo.save(t);
    }

    private Mono<Transaction> saveFailed(TransactionType type,
                                         String from,
                                         String to,
                                         BigDecimal amount,
                                         Throwable e) {
        String msg = (e instanceof ResponseStatusException rse) ? rse.getReason() : e.getMessage();
        return save(Transaction.builder()
                .type(type)
                .status(TransactionStatus.FAILED)
                .fromAccountId(from)
                .toAccountId(to)
                .amount(amount)
                .createdAt(Instant.now())
                .message(msg != null ? msg : "Fallo en operación")
                .build());
    }
}
