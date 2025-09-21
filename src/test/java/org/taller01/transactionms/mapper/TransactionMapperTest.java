package org.taller01.transactionms.mapper;

import org.junit.jupiter.api.Test;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionStatus;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.response.TransactionResponse;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapper();

    @Test
    void toResponse_ok() {
        Transaction tx = Transaction.builder()
                .id("1")
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .toAccountId("123")
                .amount(BigDecimal.valueOf(100))
                .createdAt(Instant.now())
                .message("ok")
                .build();

        TransactionResponse resp = mapper.toResponse(tx);

        assertThat(resp.getId()).isEqualTo("1");
        assertThat(resp.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(resp.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(resp.getAmount()).isEqualTo(BigDecimal.valueOf(100));
    }
}
