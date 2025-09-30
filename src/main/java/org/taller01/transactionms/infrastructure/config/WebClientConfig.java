package org.taller01.transactionms.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient accountWebClient(WebClient.Builder builder) {
    return builder.baseUrl("http://localhost:8081") // URL del microservicio AccountMS
        .build();
  }
}

