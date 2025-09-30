package org.taller01.transactionms.domain.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.taller01.transactionms.domain.exception.Messages;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.model.TransactionType;
import org.taller01.transactionms.domain.factory.TransactionFactory;
import org.taller01.transactionms.domain.port.out.AccountClientPort;
import org.taller01.transactionms.domain.port.out.ITransactionRepository;
import org.taller01.transactionms.infrastructure.external.account.AccountResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferTransaction implements TransactionStrategy<TransferRequest> {

    private final ITransactionRepository repo;
    private final AccountClientPort accountClient;
    private final TransactionFactory factory;

    @Override
    public TransactionType getType() {
        return TransactionType.TRANSFER;
    }

    @Override
    public Mono<Transaction> execute(TransferRequest req) {
        // Validación temprana: misma cuenta
        if (req.fromAccountId().equals(req.toAccountId())) {
            Transaction tx = factory.failure(
                    TransactionType.TRANSFER,
                    req.fromAccountId(),
                    req.toAccountId(),
                    req.amount(),
                    Messages.SAME_ACCOUNT_TRANSFER
            );
            return repo.save(tx);
        }

        return Mono.zip(
                        accountClient.getAccount(req.fromAccountId()),
                        accountClient.getAccount(req.toAccountId())
                )
                .flatMap(tuple -> {
                    var from = tuple.getT1();
                    var amount = req.amount();

                    if (from.balance().compareTo(amount) < 0) {
                        return repo.save(factory.failure(
                                TransactionType.TRANSFER,
                                req.fromAccountId(),
                                req.toAccountId(),
                                amount,
                                Messages.INSUFFICIENT_BALANCE
                        ));
                    }

                    return accountClient.withdraw(req.fromAccountId(), amount)
                            .then(accountClient.deposit(req.toAccountId(), amount))
                            .then(repo.save(factory.success(
                                    TransactionType.TRANSFER,
                                    req.fromAccountId(),
                                    req.toAccountId(),
                                    amount,
                                    Messages.TRANSFER_SUCCESS
                            )));
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    int statusCode = ex.getStatusCode().value();
                    String body = ex.getResponseBodyAsString();

                    log.error("❌ Error en AccountMS al transferir de {} a {}: {} - {}",
                            req.fromAccountId(), req.toAccountId(), statusCode, body, ex);

                    return repo.save(factory.failure(
                            TransactionType.TRANSFER,
                            req.fromAccountId(),
                            req.toAccountId(),
                            req.amount(),
                            "Error en AccountMS: " + statusCode + " - " + body
                    ));
                })
                .onErrorResume(e -> {
                    log.error("⚠️ Error inesperado al transferir de {} a {}: {}",
                            req.fromAccountId(), req.toAccountId(), e.getMessage(), e);

                    return repo.save(factory.failure(
                            TransactionType.TRANSFER,
                            req.fromAccountId(),
                            req.toAccountId(),
                            req.amount(),
                            e.getMessage()
                    ));
                });
    }
}
