package org.taller01.transactionms.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taller01.transactionms.common.Messages;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.factory.TransactionFactory;
import org.taller01.transactionms.port.AccountClientPort;
import org.taller01.transactionms.repository.TransactionRepository;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DepositTransaction implements TransactionStrategy<DepositRequest> {

  private final TransactionRepository repo;
  private final AccountClientPort accountClient;
  private final TransactionFactory factory;

  @Override
  public TransactionType getType() {
    return TransactionType.DEPOSIT;
  }

  @Override
  public Mono<Transaction> execute(DepositRequest req) {
    return accountClient.deposit(req.accountId(), req.amount())
        .then(repo.save(factory.success(TransactionType.DEPOSIT, null, req.accountId(),
            req.amount(), Messages.DEPOSIT_SUCCESS)))
        .onErrorResume(e -> repo.save(factory.failure(TransactionType.DEPOSIT, null,
            req.accountId(), req.amount(), e.getMessage())));
  }
}
