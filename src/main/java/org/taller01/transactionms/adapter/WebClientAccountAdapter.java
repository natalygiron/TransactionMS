package org.taller01.transactionms.adapter;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.taller01.transactionms.integration.account.AccountResponse;
import org.taller01.transactionms.port.AccountClientPort;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WebClientAccountAdapter implements AccountClientPort {

  private final WebClient webClient;

  @Override
  public Mono<Void> deposit(String accountId, BigDecimal amount) {
    return webClient.post().uri(uri -> uri.path("/cuentas/internal/{id}/deposito")
        .queryParam("amount", amount).build(accountId)).retrieve().toBodilessEntity().then();
  }

  @Override
  public Mono<Void> withdraw(String accountId, BigDecimal amount) {
    return webClient.post().uri(uri -> uri.path("/cuentas/internal/{id}/retiro")
        .queryParam("amount", amount).build(accountId)).retrieve().toBodilessEntity().then();
  }

  @Override
  public Mono<AccountResponse> getAccount(String accountId) {
    return webClient.get().uri("/cuentas/{id}", accountId).retrieve()
        .bodyToMono(AccountResponse.class);
  }
}

