package org.taller01.transactionms.domain.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.taller01.transactionms.domain.factory.TransactionFactory;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.port.out.AccountClientPort;
import org.taller01.transactionms.domain.port.out.ITransactionRepository;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.infrastructure.external.account.AccountResponse;
import org.taller01.transactionms.infrastructure.external.account.AccountType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferTransactionTest {

    private ITransactionRepository repo;
    private AccountClientPort accountClient;
    private TransferTransaction transferTx;

    @BeforeEach
    void setUp() {
        repo = mock(ITransactionRepository.class);
        accountClient = mock(AccountClientPort.class);
        transferTx = new TransferTransaction(repo, accountClient, new TransactionFactory());
    }

    @Test
    void execute_sameAccount_shouldFail() {
        var request = new TransferRequest("acc1", "acc1", BigDecimal.TEN);
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(transferTx.execute(request))
                .expectNextMatches(tx -> tx.getStatus().name().equals("FAILED"))
                .verifyComplete();
    }

    @Test
    void execute_insufficientBalance_shouldFail() {
        var request = new TransferRequest("from", "to", BigDecimal.valueOf(100));
        var from = new AccountResponse("from", "123", BigDecimal.valueOf(50), AccountType.SAVINGS, "cli");
        var to = new AccountResponse("to", "456", BigDecimal.valueOf(200), AccountType.SAVINGS, "cli");

        when(accountClient.getAccount("from")).thenReturn(Mono.just(from));
        when(accountClient.getAccount("to")).thenReturn(Mono.just(to));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(transferTx.execute(request))
                .expectNextMatches(tx -> tx.getStatus().name().equals("FAILED"))
                .verifyComplete();
    }

    @Test
    void execute_success() {
        var request = new TransferRequest("from", "to", BigDecimal.valueOf(100));
        var from = new AccountResponse("from", "123", BigDecimal.valueOf(500), AccountType.SAVINGS, "cli");
        var to = new AccountResponse("to", "456", BigDecimal.valueOf(200), AccountType.SAVINGS, "cli");

        when(accountClient.getAccount("from")).thenReturn(Mono.just(from));
        when(accountClient.getAccount("to")).thenReturn(Mono.just(to));
        when(accountClient.withdraw("from", BigDecimal.valueOf(100))).thenReturn(Mono.empty());
        when(accountClient.deposit("to", BigDecimal.valueOf(100))).thenReturn(Mono.empty());
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(transferTx.execute(request))
                .expectNextMatches(tx -> tx.getStatus().name().equals("SUCCESS"))
                .verifyComplete();
    }
}
