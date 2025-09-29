package org.taller01.transactionms.domain.port.out;

import org.taller01.transactionms.domain.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ITransactionRepository {
    Mono<Transaction> save(Transaction transaction);
    Flux<Transaction> findByAccountId(String accountId);
}
