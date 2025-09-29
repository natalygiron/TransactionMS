package org.taller01.transactionms.infrastructure.mapper;

import org.springframework.stereotype.Component;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.dto.response.TransactionResponse;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType())
                .status(tx.getStatus())
                .fromAccountId(tx.getFromAccountId())
                .toAccountId(tx.getToAccountId())
                .amount(tx.getAmount())
                .createdAt(tx.getCreatedAt())
                .message(tx.getMessage())
                .build();
    }
}
