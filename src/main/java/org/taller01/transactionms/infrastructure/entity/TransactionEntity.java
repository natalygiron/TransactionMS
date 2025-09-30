package org.taller01.transactionms.infrastructure.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.taller01.transactionms.domain.model.TransactionStatus;
import org.taller01.transactionms.domain.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

@Document("transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

  @Id
  private String id;

  private TransactionType type;
  private TransactionStatus status;
  private String fromAccountId;
  private String toAccountId;
  private BigDecimal amount;
  private Instant createdAt;
  private String message;
}
