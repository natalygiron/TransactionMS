package org.taller01.transactionms.domain.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.taller01.transactionms.domain.exception.Messages;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.model.TransactionType;
import org.taller01.transactionms.domain.factory.TransactionFactory;
import org.taller01.transactionms.domain.port.out.AccountClientPort;
import org.taller01.transactionms.domain.port.out.ITransactionRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawTransaction implements TransactionStrategy<WithdrawRequest> {

    private final ITransactionRepository repo;
    private final AccountClientPort accountClient;
    private final TransactionFactory factory;

    @Override
    public TransactionType getType() {
        return TransactionType.WITHDRAWAL;
    }

    @Override
    public Mono<Transaction> execute(WithdrawRequest req) {
        return accountClient.withdraw(req.accountId(), req.amount())
                .then(repo.save(factory.success(
                        TransactionType.WITHDRAWAL,
                        req.accountId(),
                        null,
                        req.amount(),
                        Messages.WITHDRAW_SUCCESS
                )))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    int statusCode = ex.getStatusCode().value();
                    String body = ex.getResponseBodyAsString();

                    log.error("❌ Error en AccountMS al retirar de cuenta {}: {} - {}",
                            req.accountId(), statusCode, body, ex);

                    return repo.save(factory.failure(
                            TransactionType.WITHDRAWAL,
                            req.accountId(),
                            null,
                            req.amount(),
                            "Error en AccountMS: " + statusCode + " - " + body
                    ));
                })
                .onErrorResume(e -> {
                    log.error("⚠️ Error inesperado al retirar de cuenta {}: {}",
                            req.accountId(), e.getMessage(), e);

                    return repo.save(factory.failure(
                            TransactionType.WITHDRAWAL,
                            req.accountId(),
                            null,
                            req.amount(),
                            e.getMessage()
                    ));
                });
    }
}
