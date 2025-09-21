package org.taller01.transactionms.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taller01.transactionms.common.Messages;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.factory.TransactionFactory;
import org.taller01.transactionms.integration.account.AccountResponse;
import org.taller01.transactionms.port.AccountClientPort;
import org.taller01.transactionms.repository.TransactionRepository;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TransferTransaction implements TransactionStrategy<TransferRequest> {

  private final TransactionRepository repo;
  private final AccountClientPort accountClientPort;
  private final TransactionFactory factory;

  @Override
  public TransactionType getType() {
    return TransactionType.TRANSFER;
  }

  @Override
  public Mono<Transaction> execute(TransferRequest req) {
    // ✅ Validación temprana: misma cuenta
    if (req.fromAccountId().equals(req.toAccountId())) {
      Transaction tx = factory.failure(TransactionType.TRANSFER, req.fromAccountId(),
          req.toAccountId(), req.amount(), Messages.SAME_ACCOUNT_TRANSFER);
      return repo.save(tx);
    }

    // ✅ Si son cuentas diferentes, recién consultamos en AccountMS
    return Mono.zip(accountClientPort.getAccount(req.fromAccountId()),
        accountClientPort.getAccount(req.toAccountId())).flatMap(tuple -> {
          AccountResponse from = tuple.getT1();
          BigDecimal amount = req.amount();

          // Validar saldo insuficiente
          if (from.balance().compareTo(amount) < 0) {
            return repo.save(factory.failure(TransactionType.TRANSFER, req.fromAccountId(),
                req.toAccountId(), amount, Messages.INSUFFICIENT_BALANCE));
          }

          // Flujo correcto: retiro → depósito → registrar éxito
          return accountClientPort.withdraw(req.fromAccountId(), amount)
              .then(accountClientPort.deposit(req.toAccountId(), amount))
              .then(repo.save(factory.success(TransactionType.TRANSFER, req.fromAccountId(),
                  req.toAccountId(), amount, Messages.TRANSFER_SUCCESS)))
              .onErrorResume(ex -> repo.save(factory.failure(TransactionType.TRANSFER,
                  req.fromAccountId(), req.toAccountId(), amount, Messages.TRANSFER_FAILED)));
        });
  }
}
