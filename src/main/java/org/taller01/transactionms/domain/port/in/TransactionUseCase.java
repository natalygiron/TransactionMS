package org.taller01.transactionms.domain.port.in;

import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionUseCase {
    Mono<Transaction> deposit(DepositRequest request);
    Mono<Transaction> withdraw(WithdrawRequest request);
    Mono<Transaction> transfer(TransferRequest request);
    Flux<Transaction> getHistory(String accountId);
}
