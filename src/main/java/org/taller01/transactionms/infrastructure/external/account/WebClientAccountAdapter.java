package org.taller01.transactionms.infrastructure.external.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.taller01.transactionms.domain.port.out.AccountClientPort;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WebClientAccountAdapter implements AccountClientPort {

  private final WebClient webClient;

  @Override
  public Mono<Void> deposit(String accountId, BigDecimal amount) {
    return webClient.post()
        .uri(
            uri -> uri.path("/cuentas/{id}/deposito").queryParam("amount", amount).build(accountId))
        .retrieve().toBodilessEntity().then();
  }


  @Override
  public Mono<Void> withdraw(String accountId, BigDecimal amount) {
    return webClient.post()
        .uri(uri -> uri.path("/cuentas/{id}/retiro").queryParam("amount", amount).build(accountId))
        .retrieve().toBodilessEntity().then();
  }

  @Override
  public Mono<AccountResponse> getAccount(String accountId) {
    return webClient.get().uri("/cuentas/id/{id}", accountId).retrieve()
        .bodyToMono(AccountResponse.class);
  }
}
