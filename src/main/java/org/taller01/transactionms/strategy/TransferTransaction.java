package org.taller01.transactionms.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taller01.transactionms.common.Messages;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.factory.TransactionFactory;
import org.taller01.transactionms.integration.account.AccountResponse;
import org.taller01.transactionms.port.AccountClientPort;
import org.taller01.transactionms.repository.TransactionRepository;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TransferTransaction implements TransactionStrategy<TransferRequest> {

    private final TransactionRepository repo;
    private final AccountClientPort accountClient;
    private final TransactionFactory factory;

    @Override
    public TransactionType getType() {
        return TransactionType.TRANSFER;
    }

    @Override
    public Mono<Transaction> execute(TransferRequest req) {
        if (req.fromAccountId().equals(req.toAccountId())) {
            return repo.save(factory.failure(TransactionType.TRANSFER,
                    req.fromAccountId(), req.toAccountId(),
                    req.amount(), Messages.SAME_ACCOUNT_TRANSFER));
        }

        return Mono.zip(accountClient.getAccount(req.fromAccountId()), accountClient.getAccount(req.toAccountId()))
                .flatMap(tuple -> {
                    AccountResponse source = tuple.getT1();

                    if (source.getBalance().compareTo(req.amount()) < 0) {
                        return repo.save(factory.failure(TransactionType.TRANSFER,
                                req.fromAccountId(), req.toAccountId(),
                                req.amount(), Messages.INSUFFICIENT_BALANCE));
                    }

                    return accountClient.withdraw(req.fromAccountId(), req.amount())
                            .then(accountClient.deposit(req.toAccountId(), req.amount()))
                            .then(repo.save(factory.success(TransactionType.TRANSFER,
                                    req.fromAccountId(), req.toAccountId(),
                                    req.amount(), Messages.TRANSFER_SUCCESS)));
                })
                .onErrorResume(e -> repo.save(factory.failure(TransactionType.TRANSFER,
                        req.fromAccountId(), req.toAccountId(),
                        req.amount(), e.getMessage())));
    }
}
