package org.taller01.transactionms.factory;

import org.springframework.stereotype.Component;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionStatus;
import org.taller01.transactionms.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class TransactionFactory {

    public Transaction success(TransactionType type, String fromId, String toId, BigDecimal amount, String message) {
        return Transaction.builder()
                .type(type)
                .status(TransactionStatus.SUCCESS)
                .fromAccountId(fromId)
                .toAccountId(toId)
                .amount(amount)
                .createdAt(Instant.now())
                .message(message)
                .build();
    }

    public Transaction failure(TransactionType type, String fromId, String toId, BigDecimal amount, String message) {
        return Transaction.builder()
                .type(type)
                .status(TransactionStatus.FAILED)
                .fromAccountId(fromId)
                .toAccountId(toId)
                .amount(amount)
                .createdAt(Instant.now())
                .message(message)
                .build();
    }
}
