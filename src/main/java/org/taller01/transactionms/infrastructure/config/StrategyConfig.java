package org.taller01.transactionms.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.taller01.transactionms.domain.model.TransactionType;
import org.taller01.transactionms.domain.strategy.TransactionStrategy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class StrategyConfig {

  @Bean
  public Map<TransactionType, TransactionStrategy<?>> strategyMap(
      List<TransactionStrategy<?>> strategies) {
    return strategies.stream().collect(Collectors.toMap(TransactionStrategy::getType, s -> s));
  }
}
