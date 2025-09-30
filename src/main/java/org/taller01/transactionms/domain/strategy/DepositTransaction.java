package org.taller01.transactionms.domain.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.taller01.transactionms.domain.exception.Messages;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.model.TransactionType;
import org.taller01.transactionms.domain.factory.TransactionFactory;
import org.taller01.transactionms.domain.port.out.AccountClientPort;
import org.taller01.transactionms.domain.port.out.ITransactionRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositTransaction implements TransactionStrategy<DepositRequest> {

  private final ITransactionRepository repo;
  private final AccountClientPort accountClient;
  private final TransactionFactory factory;

  @Override
  public TransactionType getType() {
    return TransactionType.DEPOSIT;
  }

  @Override
  public Mono<Transaction> execute(DepositRequest req) {
    return accountClient.deposit(req.accountId(), req.amount()).then(repo.save(factory.success( // 👈
                                                                                                // cambiamos
                                                                                                // flatMap
                                                                                                // ->
                                                                                                // then
        TransactionType.DEPOSIT, null, req.accountId(), req.amount(), Messages.DEPOSIT_SUCCESS)))
        .onErrorResume(ex -> {
          if (ex instanceof WebClientResponseException wcre) {
            int statusCode = wcre.getStatusCode().value();
            String body = wcre.getResponseBodyAsString();

            log.error("❌ Error en AccountMS al depositar en cuenta {}: {} - {}", req.accountId(),
                statusCode, body, wcre);

            return repo.save(factory.failure(TransactionType.DEPOSIT, null, req.accountId(),
                req.amount(), "Error en AccountMS: " + statusCode + " - " + body));
          }

          log.error("⚠️ Error inesperado al depositar en cuenta {}: {}", req.accountId(),
              ex.getMessage(), ex);

          return repo.save(factory.failure(TransactionType.DEPOSIT, null, req.accountId(),
              req.amount(), ex.getMessage()));
        });
  }
}
