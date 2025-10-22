package com.bank.transactions.infrastructure.config;

import com.bank.transactions.domain.model.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

/**
 * Configuración del sink reactivo utilizado para emitir eventos de transacciones.
 */
@Configuration
public class SinkConfig {

  /**
   * Define un {@link Sinks.Many} para publicar eventos de tipo {@link Transaction}.
   * Usa una política de multicast con buffer para manejar presión de backpressure.
   *
   * @return instancia configurada de {@link Sinks.Many}
   */
  @Bean
  public Sinks.Many<Transaction> txSink() {
    return Sinks.many()
      .multicast()
      .onBackpressureBuffer(1000);
  }
}
