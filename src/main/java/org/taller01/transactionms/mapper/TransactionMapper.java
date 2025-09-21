package org.taller01.transactionms.mapper;

import org.springframework.stereotype.Component;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.dto.response.TransactionResponse;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .type(t.getType())
                .status(t.getStatus())
                .fromAccountId(t.getFromAccountId())
                .toAccountId(t.getToAccountId())
                .amount(t.getAmount())
                .createdAt(t.getCreatedAt())
                .message(t.getMessage())
                .build();
    }
}