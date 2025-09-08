package org.taller01.transactionms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI transactionOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("API de Transacciones Bancarias").version("1.0.0").description(
            "Microservicio para registrar depósitos, retiros, transferencias y consultar historial de cuentas"));
  }
}
