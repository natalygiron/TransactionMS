package org.taller01.transactionms.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taller01.transactionms.common.Messages;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.factory.TransactionFactory;
import org.taller01.transactionms.port.AccountClientPort;
import org.taller01.transactionms.repository.TransactionRepository;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WithdrawTransaction implements TransactionStrategy<WithdrawRequest> {

  private final TransactionRepository repo;
  private final AccountClientPort accountClient;
  private final TransactionFactory factory;

  @Override
  public TransactionType getType() {
    return TransactionType.WITHDRAWAL;
  }

  @Override
  public Mono<Transaction> execute(WithdrawRequest req) {
    return accountClient.withdraw(req.accountId(), req.amount())
        .then(repo.save(factory.success(TransactionType.WITHDRAWAL, req.accountId(), null,
            req.amount(), Messages.WITHDRAW_SUCCESS)))
        .onErrorResume(e -> repo.save(factory.failure(TransactionType.WITHDRAWAL, req.accountId(),
            null, req.amount(), e.getMessage())));
  }
}
