package org.taller01.transactionms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.taller01.transactionms.domain.TransactionType;
import org.taller01.transactionms.strategy.TransactionStrategy;
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
