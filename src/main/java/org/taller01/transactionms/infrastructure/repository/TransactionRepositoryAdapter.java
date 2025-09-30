package org.taller01.transactionms.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.port.out.ITransactionRepository;
import org.taller01.transactionms.infrastructure.entity.TransactionEntity;
import org.taller01.transactionms.infrastructure.mapper.TransactionEntityMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements ITransactionRepository {

  private final TransactionRepository mongoRepository;
  private final TransactionEntityMapper mapper;

  @Override
  public Mono<Transaction> save(Transaction transaction) {
    TransactionEntity entity = mapper.toEntity(transaction);
    return mongoRepository.save(entity).map(mapper::toDomain);
  }

  @Override
  public Flux<Transaction> findByAccountId(String accountId) {
    return mongoRepository.findByFromAccountIdOrToAccountId(accountId, accountId)
        .map(mapper::toDomain);
  }
}
