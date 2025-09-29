package org.taller01.transactionms.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.model.TransactionType;
import org.taller01.transactionms.domain.port.in.TransactionUseCase;
import org.taller01.transactionms.domain.port.out.ITransactionRepository;
import org.taller01.transactionms.domain.strategy.TransactionStrategy;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionService implements TransactionUseCase {

  private final Map<TransactionType, TransactionStrategy<?>> strategyMap;
  private final ITransactionRepository transactionRepository; // 👈 inyección directa

  @SuppressWarnings("unchecked")
  private <T> TransactionStrategy<T> getStrategy(TransactionType type) {
    TransactionStrategy<?> strategy = strategyMap.get(type);
    if (strategy == null) {
      throw new IllegalArgumentException("Tipo de transacción no soportado: " + type);
    }
    return (TransactionStrategy<T>) strategy;
  }

  @Override
  public Mono<Transaction> deposit(DepositRequest request) {
    return getStrategy(TransactionType.DEPOSIT).execute(request);
  }

  @Override
  public Mono<Transaction> withdraw(WithdrawRequest request) {
    return getStrategy(TransactionType.WITHDRAWAL).execute(request);
  }

  @Override
  public Mono<Transaction> transfer(TransferRequest request) {
    return getStrategy(TransactionType.TRANSFER).execute(request);
  }

  @Override
  public Flux<Transaction> getHistory(String accountId) {
    // ✅ directo desde el repo
    return transactionRepository.findByAccountId(accountId);
  }
}

