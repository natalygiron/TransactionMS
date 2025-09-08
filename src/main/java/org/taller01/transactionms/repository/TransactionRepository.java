package org.taller01.transactionms.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.taller01.transactionms.domain.Transaction;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findByFromAccountIdOrToAccountId(String fromId, String toId);
}