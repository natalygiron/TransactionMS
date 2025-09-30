package org.taller01.transactionms.infrastructure.mapper;

import org.springframework.stereotype.Component;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.infrastructure.entity.TransactionEntity;

@Component
public class TransactionEntityMapper {

  public TransactionEntity toEntity(Transaction tx) {
    return TransactionEntity.builder().id(tx.getId()).type(tx.getType()).status(tx.getStatus())
        .fromAccountId(tx.getFromAccountId()).toAccountId(tx.getToAccountId())
        .amount(tx.getAmount()).createdAt(tx.getCreatedAt()).message(tx.getMessage()).build();
  }

  public Transaction toDomain(TransactionEntity entity) {
    return Transaction.builder().id(entity.getId()).type(entity.getType())
        .status(entity.getStatus()).fromAccountId(entity.getFromAccountId())
        .toAccountId(entity.getToAccountId()).amount(entity.getAmount())
        .createdAt(entity.getCreatedAt()).message(entity.getMessage()).build();
  }
}
