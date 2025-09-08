package org.taller01.transactionms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.taller01.transactionms.domain.Transaction;
import org.taller01.transactionms.domain.TransactionStatus;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.dto.request.DepositRequest;
import org.taller01.transactionms.repository.TransactionRepository;
import org.taller01.transactionms.service.TransactionService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repo;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private TransactionService service;

    @Test
    void shouldRegisterDepositSuccessfully() {
        DepositRequest req = new DepositRequest("account-id", BigDecimal.valueOf(100));

        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(any(Function.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);

        // Encadenar onStatus múltiples veces
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));

        Transaction tx = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .toAccountId("account-id")
                .amount(BigDecimal.valueOf(100))
                .createdAt(Instant.now())
                .message("Depósito aplicado")
                .build();

        when(repo.save(any(Transaction.class))).thenReturn(Mono.just(tx));

        StepVerifier.create(service.deposit(req))
                .expectNextMatches(t -> t.getStatus() == TransactionStatus.SUCCESS && t.getAmount().equals(BigDecimal.valueOf(100)))
                .verifyComplete();
    }
}
