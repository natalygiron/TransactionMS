package org.taller01.transactionms.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.taller01.transactionms.common.Messages;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.factory.TransactionFactory;
import org.taller01.transactionms.port.AccountClientPort;
import org.taller01.transactionms.repository.TransactionRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DepositTransactionTest {

    private TransactionRepository repo;
    private AccountClientPort accountClient;
    private TransactionFactory factory;
    private DepositTransaction depositTransaction;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(TransactionRepository.class);
        accountClient = Mockito.mock(AccountClientPort.class);
        factory = new TransactionFactory();
        depositTransaction = new DepositTransaction(repo, accountClient, factory);
    }

    @Test
    void execute_success() {
        DepositRequest req = new DepositRequest("123", BigDecimal.valueOf(100));
        Transaction tx = factory.success(TransactionType.DEPOSIT, null, "123", req.amount(), Messages.DEPOSIT_SUCCESS);

        when(accountClient.deposit(any(), any())).thenReturn(Mono.empty());
        when(repo.save(any(Transaction.class))).thenReturn(Mono.just(tx));

        StepVerifier.create(depositTransaction.execute(req))
                .expectNextMatches(t -> t.getStatus().name().equals("SUCCESS"))
                .verifyComplete();

        verify(accountClient).deposit("123", req.amount());
        verify(repo).save(any(Transaction.class));
    }
}
