package org.taller01.transactionms.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.taller01.transactionms.domain.model.Transaction;
import org.taller01.transactionms.domain.model.TransactionType;
import org.taller01.transactionms.domain.port.out.ITransactionRepository;
import org.taller01.transactionms.domain.strategy.TransactionStrategy;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.dto.request.TransferRequest;
import org.taller01.transactionms.dto.request.WithdrawRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private ITransactionRepository transactionRepository;
    private TransactionStrategy<DepositRequest> depositStrategy;
    private TransactionStrategy<WithdrawRequest> withdrawStrategy;
    private TransactionStrategy<TransferRequest> transferStrategy;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(ITransactionRepository.class);

        depositStrategy = mock(TransactionStrategy.class);
        when(depositStrategy.getType()).thenReturn(TransactionType.DEPOSIT);

        withdrawStrategy = mock(TransactionStrategy.class);
        when(withdrawStrategy.getType()).thenReturn(TransactionType.WITHDRAWAL);

        transferStrategy = mock(TransactionStrategy.class);
        when(transferStrategy.getType()).thenReturn(TransactionType.TRANSFER);

        service = new TransactionService(
                Map.of(
                        TransactionType.DEPOSIT, depositStrategy,
                        TransactionType.WITHDRAWAL, withdrawStrategy,
                        TransactionType.TRANSFER, transferStrategy
                ),
                transactionRepository
        );
    }

    @Test
    void deposit_shouldDelegateToDepositStrategy() {
        var request = new DepositRequest("acc1", BigDecimal.valueOf(100));
        var tx = Transaction.builder().id("tx1").type(TransactionType.DEPOSIT).build();

        when(depositStrategy.execute(any())).thenReturn(Mono.just(tx));

        StepVerifier.create(service.deposit(request))
                .expectNext(tx)
                .verifyComplete();

        verify(depositStrategy).execute(request);
    }

    @Test
    void withdraw_shouldDelegateToWithdrawStrategy() {
        var request = new WithdrawRequest("acc1", BigDecimal.valueOf(50));
        var tx = Transaction.builder().id("tx2").type(TransactionType.WITHDRAWAL).build();

        when(withdrawStrategy.execute(any())).thenReturn(Mono.just(tx));

        StepVerifier.create(service.withdraw(request))
                .expectNext(tx)
                .verifyComplete();

        verify(withdrawStrategy).execute(request);
    }

    @Test
    void transfer_shouldDelegateToTransferStrategy() {
        var request = new TransferRequest("acc1", "acc2", BigDecimal.valueOf(200));
        var tx = Transaction.builder().id("tx3").type(TransactionType.TRANSFER).build();

        when(transferStrategy.execute(any())).thenReturn(Mono.just(tx));

        StepVerifier.create(service.transfer(request))
                .expectNext(tx)
                .verifyComplete();

        verify(transferStrategy).execute(request);
    }

    @Test
    void getHistory_shouldDelegateToRepository() {
        var tx = Transaction.builder().id("tx4").build();
        when(transactionRepository.findByAccountId("acc1"))
                .thenReturn(Flux.just(tx));

        StepVerifier.create(service.getHistory("acc1"))
                .expectNext(tx)
                .verifyComplete();

        verify(transactionRepository).findByAccountId("acc1");
    }

    @Test
    void getStrategy_shouldThrowWhenNotFound() {
        var serviceWithoutStrategies = new TransactionService(Map.of(), transactionRepository);

        var ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> serviceWithoutStrategies.deposit(new DepositRequest("acc1", BigDecimal.TEN)).block()
        );

        org.assertj.core.api.Assertions.assertThat(ex.getMessage())
                .contains("Tipo de transacción no soportado");
    }

}
