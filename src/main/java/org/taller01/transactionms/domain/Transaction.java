package org.taller01.transactionms.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("transactions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id
    private String id;
    private TransactionType type;
    private TransactionStatus status;

    private String fromAccountId;   // null para depósito
    private String toAccountId;     // null para retiro
    private BigDecimal amount;
    private Instant createdAt;
    private String message;         // detalle/observación
}