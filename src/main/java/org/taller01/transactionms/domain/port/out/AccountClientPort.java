package org.taller01.transactionms.domain.port.out;

import org.taller01.transactionms.infrastructure.external.account.AccountResponse;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

public interface AccountClientPort {
  Mono<Void> deposit(String accountId, BigDecimal amount);

  Mono<Void> withdraw(String accountId, BigDecimal amount);

  Mono<AccountResponse> getAccount(String accountId);
}
