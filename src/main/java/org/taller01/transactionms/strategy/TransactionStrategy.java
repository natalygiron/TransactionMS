package org.taller01.transactionms.strategy;

import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionType;
import reactor.core.publisher.Mono;

public interface TransactionStrategy<T> {
    TransactionType getType();
    Mono<Transaction> execute(T request);
}