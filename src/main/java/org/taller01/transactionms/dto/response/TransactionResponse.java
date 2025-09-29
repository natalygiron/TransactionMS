package org.taller01.transactionms.dto.response;

import lombok.*;
import org.taller01.transactionms.domain.model.TransactionStatus;
import org.taller01.transactionms.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
  private String id;
  private TransactionType type;
  private TransactionStatus status;
  private String fromAccountId;
  private String toAccountId;
  private BigDecimal amount;
  private Instant createdAt;
  private String message;
}
