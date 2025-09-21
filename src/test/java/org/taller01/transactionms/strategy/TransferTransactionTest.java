package org.taller01.transactionms.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.taller01.transactionms.common.Messages;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.factory.TransactionFactory;
import org.taller01.transactionms.integration.account.AccountResponse;
import org.taller01.transactionms.port.AccountClientPort;
import org.taller01.transactionms.repository.TransactionRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferTransactionTest {

    private TransactionRepository repo;
    private AccountClientPort accountClient;
    private TransactionFactory factory;
    private TransferTransaction transferTransaction;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(TransactionRepository.class);
        accountClient = Mockito.mock(AccountClientPort.class);
        factory = new TransactionFactory();
        transferTransaction = new TransferTransaction(repo, accountClient, factory);
    }

    @Test
    void execute_success() {
        TransferRequest req = new TransferRequest("a1", "a2", BigDecimal.valueOf(100));
        AccountResponse from = new AccountResponse("a1", "123", BigDecimal.valueOf(200), null, "c1");
        AccountResponse to = new AccountResponse("a2", "456", BigDecimal.valueOf(50), null, "c2");
        Transaction tx = factory.success(TransactionType.TRANSFER, "a1", "a2", req.amount(), Messages.TRANSFER_SUCCESS);

        when(accountClient.getAccount("a1")).thenReturn(Mono.just(from));
        when(accountClient.getAccount("a2")).thenReturn(Mono.just(to));
        when(accountClient.withdraw("a1", req.amount())).thenReturn(Mono.empty());
        when(accountClient.deposit("a2", req.amount())).thenReturn(Mono.empty());
        when(repo.save(any(Transaction.class))).thenReturn(Mono.just(tx));

        StepVerifier.create(transferTransaction.execute(req))
                .expectNextMatches(t -> t.getStatus().name().equals("SUCCESS"))
                .verifyComplete();

        verify(accountClient).withdraw("a1", req.amount());
        verify(accountClient).deposit("a2", req.amount());
        verify(repo).save(any(Transaction.class));
    }

    @Test
    void execute_insufficientBalance() {
        TransferRequest req = new TransferRequest("a1", "a2", BigDecimal.valueOf(500));
        AccountResponse from = new AccountResponse("a1", "123", BigDecimal.valueOf(100), null, "c1");
        AccountResponse to = new AccountResponse("a2", "456", BigDecimal.valueOf(50), null, "c2");
        Transaction tx = factory.failure(TransactionType.TRANSFER, "a1", "a2", req.amount(), Messages.INSUFFICIENT_BALANCE);

        when(accountClient.getAccount("a1")).thenReturn(Mono.just(from));
        when(accountClient.getAccount("a2")).thenReturn(Mono.just(to));
        when(repo.save(any(Transaction.class))).thenReturn(Mono.just(tx));

        StepVerifier.create(transferTransaction.execute(req))
                .expectNextMatches(t -> t.getStatus().name().equals("FAILED") &&
                        t.getMessage().equals(Messages.INSUFFICIENT_BALANCE))
                .verifyComplete();

        verify(repo).save(any(Transaction.class));
        verify(accountClient, never()).withdraw(any(), any());
        verify(accountClient, never()).deposit(any(), any());
    }
}
