package org.taller01.transactionms.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad de dominio pura (sin dependencias de frameworks).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    private String id;
    private TransactionType type;
    private TransactionStatus status;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private Instant createdAt;
    private String message;
}
