package org.taller01.transactionms.domain.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.taller01.transactionms.domain.exception.Messages;
import org.taller01.transactionms.domain.factory.TransactionFactory;
import org.taller01.transactionms.domain.model.TransactionType;
import org.taller01.transactionms.domain.port.out.AccountClientPort;
import org.taller01.transactionms.domain.port.out.ITransactionRepository;
import org.taller01.transactionms.dto.request.DepositRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DepositTransactionTest {

    private ITransactionRepository repo;
    private AccountClientPort accountClient;
    private TransactionFactory factory;
    private DepositTransaction depositTx;

    @BeforeEach
    void setUp() {
        repo = mock(ITransactionRepository.class);
        accountClient = mock(AccountClientPort.class);
        factory = new TransactionFactory();
        depositTx = new DepositTransaction(repo, accountClient, factory);
    }

    @Test
    void execute_shouldSaveSuccessTransaction() {
        var request = new DepositRequest("acc1", BigDecimal.TEN);

        // ✅ Ahora usamos Mono.empty() porque .then(...) lo maneja bien
        when(accountClient.deposit("acc1", BigDecimal.TEN))
                .thenReturn(Mono.empty());

        when(repo.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(depositTx.execute(request))
                .expectNextMatches(tx ->
                        tx.getStatus().toString().equals("SUCCESS") &&
                                tx.getType() == TransactionType.DEPOSIT &&
                                tx.getToAccountId().equals("acc1") &&
                                tx.getMessage().equals(Messages.DEPOSIT_SUCCESS)
                )
                .verifyComplete();
    }




    @Test
    void execute_shouldHandleWebClientResponseException() {
        var request = new DepositRequest("acc1", BigDecimal.TEN);

        var ex = WebClientResponseException.create(
                404, "Not Found", null,
                "account not found".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(accountClient.deposit("acc1", BigDecimal.TEN))
                .thenReturn(Mono.error(ex));

        when(repo.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(depositTx.execute(request))
                .expectNextMatches(tx ->
                        tx.getStatus().toString().equals("FAILED") &&
                                tx.getMessage().contains("Error en AccountMS")
                )
                .verifyComplete();
    }

}
