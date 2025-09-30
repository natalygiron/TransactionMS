package org.taller01.transactionms.domain.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.taller01.transactionms.domain.factory.TransactionFactory;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.port.out.AccountClientPort;
import org.taller01.transactionms.domain.port.out.ITransactionRepository;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WithdrawTransactionTest {

    private ITransactionRepository repo;
    private AccountClientPort accountClient;
    private WithdrawTransaction withdrawTx;

    @BeforeEach
    void setUp() {
        repo = mock(ITransactionRepository.class);
        accountClient = mock(AccountClientPort.class);
        withdrawTx = new WithdrawTransaction(repo, accountClient, new TransactionFactory());
    }

    @Test
    void execute_success() {
        var request = new WithdrawRequest("acc1", BigDecimal.TEN);
        when(accountClient.withdraw("acc1", BigDecimal.TEN)).thenReturn(Mono.empty());
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(withdrawTx.execute(request))
                .expectNextMatches(tx -> tx.getStatus().name().equals("SUCCESS"))
                .verifyComplete();
    }

    @Test
    void execute_failure() {
        var request = new WithdrawRequest("acc1", BigDecimal.TEN);
        var ex = WebClientResponseException.create(400, "Bad Request", null,
                "fail".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        when(accountClient.withdraw("acc1", BigDecimal.TEN)).thenReturn(Mono.error(ex));
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(withdrawTx.execute(request))
                .expectNextMatches(tx -> tx.getStatus().name().equals("FAILED"))
                .verifyComplete();
    }
}
