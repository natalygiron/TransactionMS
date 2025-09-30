package org.taller01.transactionms.domain.strategy;

import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.model.TransactionType;
import reactor.core.publisher.Mono;

public interface TransactionStrategy<T> {
  TransactionType getType();
  Mono<Transaction> execute(T request);
}
