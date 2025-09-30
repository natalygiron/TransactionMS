package org.taller01.transactionms.infrastructure.mapper;

import org.junit.jupiter.api.Test;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.model.TransactionStatus;
import org.taller01.transactionms.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapper();

    @Test
    void toResponse_shouldMapCorrectly() {
        var tx = Transaction.builder()
                .id("id1")
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.FAILED)
                .fromAccountId("a1")
                .amount(BigDecimal.ONE)
                .createdAt(Instant.now())
                .message("fail")
                .build();

        var response = mapper.toResponse(tx);

        assertEquals("id1", response.getId());
        assertEquals(TransactionType.WITHDRAWAL, response.getType());
        assertEquals(TransactionStatus.FAILED, response.getStatus());
    }
}
