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
import org.taller01.transactionms.integration.account.AccountType;
import org.taller01.transactionms.port.AccountClientPort;
import org.taller01.transactionms.repository.TransactionRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferTransactionTest {

    private TransactionRepository repo;
    private AccountClientPort accountClientPort;
    private TransactionFactory factory;
    private TransferTransaction transferTransaction;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(TransactionRepository.class);
        accountClientPort = Mockito.mock(AccountClientPort.class);
        factory = new TransactionFactory();
        transferTransaction = new TransferTransaction(repo, accountClientPort, factory);
    }

    @Test
    void execute_success() {
        TransferRequest req = new TransferRequest("a1", "a2", BigDecimal.valueOf(100));

        AccountResponse from = new AccountResponse("a1", "123", BigDecimal.valueOf(200), AccountType.SAVINGS, "c1");
        AccountResponse to = new AccountResponse("a2", "456", BigDecimal.valueOf(50), AccountType.CHECKING, "c2");

        Transaction tx = factory.success(TransactionType.TRANSFER, "a1", "a2", req.amount(), Messages.TRANSFER_SUCCESS);

        when(accountClientPort.getAccount("a1")).thenReturn(Mono.just(from));
        when(accountClientPort.getAccount("a2")).thenReturn(Mono.just(to));
        when(accountClientPort.withdraw("a1", req.amount())).thenReturn(Mono.empty());
        when(accountClientPort.deposit("a2", req.amount())).thenReturn(Mono.empty());
        when(repo.save(any(Transaction.class))).thenReturn(Mono.just(tx));

        StepVerifier.create(transferTransaction.execute(req))
                .expectNextMatches(t -> t.getStatus().name().equals("SUCCESS"))
                .verifyComplete();

        verify(accountClientPort).withdraw("a1", req.amount());
        verify(accountClientPort).deposit("a2", req.amount());
        verify(repo).save(any(Transaction.class));
    }

    @Test
    void execute_insufficientBalance() {
        TransferRequest req = new TransferRequest("a1", "a2", BigDecimal.valueOf(500));

        AccountResponse from = new AccountResponse("a1", "123", BigDecimal.valueOf(100), AccountType.SAVINGS, "c1");
        AccountResponse to = new AccountResponse("a2", "456", BigDecimal.valueOf(50), AccountType.CHECKING, "c2");

        Transaction tx = factory.failure(TransactionType.TRANSFER, "a1", "a2", req.amount(), Messages.INSUFFICIENT_BALANCE);

        when(accountClientPort.getAccount("a1")).thenReturn(Mono.just(from));
        when(accountClientPort.getAccount("a2")).thenReturn(Mono.just(to));
        when(repo.save(any(Transaction.class))).thenReturn(Mono.just(tx));

        StepVerifier.create(transferTransaction.execute(req))
                .expectNextMatches(t ->
                        t.getStatus().name().equals("FAILED") &&
                                t.getMessage().equals(Messages.INSUFFICIENT_BALANCE))
                .verifyComplete();

        verify(repo).save(any(Transaction.class));
        verify(accountClientPort, never()).withdraw(any(), any());
        verify(accountClientPort, never()).deposit(any(), any());
    }

    @Test
    void execute_sameAccount_shouldFail() {
        TransferRequest req = new TransferRequest("a1", "a1", BigDecimal.valueOf(100));

        Transaction tx = factory.failure(TransactionType.TRANSFER, "a1", "a1", req.amount(), Messages.SAME_ACCOUNT_TRANSFER);

        when(repo.save(any(Transaction.class))).thenReturn(Mono.just(tx));

        StepVerifier.create(transferTransaction.execute(req))
                .expectNextMatches(t ->
                        t.getStatus().name().equals("FAILED") &&
                                t.getMessage().equals(Messages.SAME_ACCOUNT_TRANSFER))
                .verifyComplete();

        verify(repo).save(any(Transaction.class));
        verify(accountClientPort, never()).getAccount(any());
        verify(accountClientPort, never()).withdraw(any(), any());
        verify(accountClientPort, never()).deposit(any(), any());
    }
}
