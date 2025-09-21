package org.taller01.transactionms.factory;

import org.junit.jupiter.api.Test;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionStatus;
import org.taller01.transactionms.domain.TransactionType;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionFactoryTest {

    private final TransactionFactory factory = new TransactionFactory();

    @Test
    void success_createsTransaction() {
        Transaction tx = factory.success(TransactionType.DEPOSIT, null, "acc1", BigDecimal.TEN, "ok");
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(tx.getAmount()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    void failure_createsTransaction() {
        Transaction tx = factory.failure(TransactionType.TRANSFER, "a1", "a2", BigDecimal.ONE, "fail");
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(tx.getMessage()).isEqualTo("fail");
    }
}
