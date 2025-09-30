package org.taller01.transactionms.infrastructure.mapper;

import org.junit.jupiter.api.Test;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.model.TransactionStatus;
import org.taller01.transactionms.domain.model.TransactionType;
import org.taller01.transactionms.infrastructure.entity.TransactionEntity;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityMapperTest {

    private final TransactionEntityMapper mapper = new TransactionEntityMapper();

    @Test
    void toEntity_and_back_shouldBeEqual() {
        var tx = Transaction.builder()
                .id("id1")
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .fromAccountId("a1")
                .toAccountId("a2")
                .amount(BigDecimal.TEN)
                .createdAt(Instant.now())
                .message("ok")
                .build();

        TransactionEntity entity = mapper.toEntity(tx);
        Transaction back = mapper.toDomain(entity);

        assertEquals(tx.getId(), back.getId());
        assertEquals(tx.getType(), back.getType());
    }
}
