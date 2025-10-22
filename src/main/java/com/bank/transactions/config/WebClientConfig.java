package com.bank.transactions.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración del cliente WebClient para realizar llamadas HTTP reactivas
 * al servicio de riesgo simulado.
 */
@Configuration
public class WebClientConfig {

  /**
   * Crea un {@link WebClient} configurado para el servicio mock de riesgo.
   *
   * @return instancia de WebClient configurada.
   */
  @Bean
  public WebClient riskWebClient() {
    return WebClient.builder()
      .baseUrl("http://localhost:8070/mock/risk")
      .build();
  }
}