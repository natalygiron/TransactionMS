package org.taller01.transactionms.port;

import org.taller01.transactionms.integration.account.AccountResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountClientPort {
    Mono<Void> deposit(String accountId, BigDecimal amount);
    Mono<Void> withdraw(String accountId, BigDecimal amount);
    Mono<AccountResponse> getAccount(String accountId);
}
