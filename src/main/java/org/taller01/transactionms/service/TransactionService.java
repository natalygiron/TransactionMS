package org.taller01.transactionms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.dto.response.TransactionResponse;
import org.taller01.transactionms.mapper.TransactionMapper;
import org.taller01.transactionms.repository.TransactionRepository;
import org.taller01.transactionms.strategy.TransactionStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionService {

  private final TransactionRepository repo;
  private final Map<TransactionType, TransactionStrategy<?>> strategyMap;
  private final TransactionMapper mapper;

  @SuppressWarnings("unchecked")
  private <T> TransactionStrategy<T> getStrategy(TransactionType type) {
    TransactionStrategy<?> strategy = strategyMap.get(type);
    if (strategy == null) {
      throw new IllegalArgumentException("Tipo de transacción no soportado: " + type);
    }
    return (TransactionStrategy<T>) strategy;
  }

  public Mono<Transaction> deposit(DepositRequest req) {
    return getStrategy(TransactionType.DEPOSIT).execute(req);
  }

  public Mono<Transaction> withdraw(WithdrawRequest req) {
    return getStrategy(TransactionType.WITHDRAWAL).execute(req);
  }

  public Mono<Transaction> transfer(TransferRequest req) {
    return getStrategy(TransactionType.TRANSFER).execute(req);
  }

  public Flux<TransactionResponse> getHistory(String accountId) {
    return repo.findByFromAccountIdOrToAccountId(accountId, accountId)
            .map(mapper::toResponse);
  }
}
