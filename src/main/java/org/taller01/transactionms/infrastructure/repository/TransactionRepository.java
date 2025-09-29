package org.taller01.transactionms.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.taller01.transactionms.infrastructure.entity.TransactionEntity;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<TransactionEntity, String> {
  Flux<TransactionEntity> findByFromAccountIdOrToAccountId(String fromId, String toId);
}
