package org.taller01.transactionms.dto.response;

import lombok.*;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionStatus;
import org.taller01.transactionms.domain.TransactionType;

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

  public static TransactionResponse from(Transaction t) {
    return TransactionResponse.builder().id(t.getId()).type(t.getType()).status(t.getStatus())
        .fromAccountId(t.getFromAccountId()).toAccountId(t.getToAccountId()).amount(t.getAmount())
        .createdAt(t.getCreatedAt()).message(t.getMessage()).build();
  }
}
