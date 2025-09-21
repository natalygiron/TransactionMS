package org.taller01.transactionms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionStatus;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import org.taller01.transactionms.repository.TransactionRepository;
import org.taller01.transactionms.strategy.TransactionStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repo;

    @Mock
    private TransactionStrategy<DepositRequest> depositStrategy;

    @Mock
    private TransactionStrategy<WithdrawRequest> withdrawStrategy;

    @Mock
    private TransactionStrategy<TransferRequest> transferStrategy;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        Map<TransactionType, TransactionStrategy<?>> strategyMap = Map.of(
                TransactionType.DEPOSIT, depositStrategy,
                TransactionType.WITHDRAWAL, withdrawStrategy,
                TransactionType.TRANSFER, transferStrategy
        );
        service = new TransactionService(repo, strategyMap);
    }

    private Transaction sampleTx(String id, TransactionType type, BigDecimal amount) {
        return Transaction.builder()
                .id(id)
                .type(type)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void deposit_executesStrategy() {
        DepositRequest req = new DepositRequest("acc1", BigDecimal.valueOf(100));
        Transaction tx = sampleTx("1", TransactionType.DEPOSIT, req.amount());

        when(depositStrategy.execute(req)).thenReturn(Mono.just(tx));

        StepVerifier.create(service.deposit(req))
                .expectNext(tx)
                .verifyComplete();
    }

    @Test
    void withdraw_executesStrategy() {
        WithdrawRequest req = new WithdrawRequest("acc1", BigDecimal.valueOf(50));
        Transaction tx = sampleTx("2", TransactionType.WITHDRAWAL, req.amount());

        when(withdrawStrategy.execute(req)).thenReturn(Mono.just(tx));

        StepVerifier.create(service.withdraw(req))
                .expectNext(tx)
                .verifyComplete();
    }

    @Test
    void transfer_executesStrategy() {
        TransferRequest req = new TransferRequest("a1", "a2", BigDecimal.valueOf(200));
        Transaction tx = sampleTx("3", TransactionType.TRANSFER, req.amount());

        when(transferStrategy.execute(req)).thenReturn(Mono.just(tx));

        StepVerifier.create(service.transfer(req))
                .expectNext(tx)
                .verifyComplete();
    }

    @Test
    void getHistory_returnsTransactions() {
        Transaction tx1 = sampleTx("4", TransactionType.DEPOSIT, BigDecimal.valueOf(100));
        Transaction tx2 = sampleTx("5", TransactionType.WITHDRAWAL, BigDecimal.valueOf(30));

        when(repo.findByFromAccountIdOrToAccountId("acc1", "acc1"))
                .thenReturn(Flux.just(tx1, tx2));

        StepVerifier.create(service.getHistory("acc1"))
                .expectNext(tx1)
                .expectNext(tx2)
                .verifyComplete();
    }
}
