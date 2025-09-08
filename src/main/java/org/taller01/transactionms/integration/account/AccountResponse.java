package org.taller01.transactionms.integration.account;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
  private String id;
  private String accountNumber;
  private BigDecimal balance;
  private AccountType type;
  private String clientId;
}
