package org.taller01.transactionms.infrastructure.external.account;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;

class WebClientAccountAdapterTest {

    private MockWebServer mockWebServer;
    private WebClientAccountAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient client = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        adapter = new WebClientAccountAdapter(client);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void deposit_shouldReturnVoidOnSuccess() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        StepVerifier.create(adapter.deposit("acc1", BigDecimal.TEN))
                .verifyComplete();
    }

    @Test
    void withdraw_shouldReturnVoidOnSuccess() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        StepVerifier.create(adapter.withdraw("acc1", BigDecimal.TEN))
                .verifyComplete();
    }

    @Test
    void getAccount_shouldReturnAccountResponse() {
        String json = """
                {
                  "id": "acc1",
                  "accountNumber": "123456",
                  "balance": 1000.0,
                  "type": "SAVINGS",
                  "clientId": "client1"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.getAccount("acc1"))
                .expectNextMatches(account ->
                        account.id().equals("acc1") &&
                                account.accountNumber().equals("123456") &&
                                account.balance().compareTo(BigDecimal.valueOf(1000.0)) == 0 &&
                                account.type().toString().equals("SAVINGS") &&
                                account.clientId().equals("client1")
                )
                .verifyComplete();
    }
}
