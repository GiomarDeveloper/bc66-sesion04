package com.bank.transactions.application.service;

import com.bank.transactions.domain.model.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

/**
 * Configuración de beans relacionados con los flujos reactivos del sistema.
 *
 * <p>Define un {@link Sinks.Many} para publicar eventos de transacciones,
 * permitiendo que múltiples suscriptores reaccionen a las operaciones de
 * {@link Transaction} en tiempo real.</p>
 */
@Configuration
public class SinksConfig {

  /**
   * Crea e inyecta un sink reactivo multicast con buffer ante presión de backpressure.
   *
   * <p>Este sink se utiliza para emitir eventos de transacciones a múltiples
   * suscriptores de manera concurrente, garantizando que ningún evento se pierda.</p>
   *
   * @return un bean de tipo {@link Sinks.Many} para manejar flujos de {@link Transaction}
   */
  @Bean
  public Sinks.Many<Transaction> transactionSink() {
    return Sinks.many().multicast().onBackpressureBuffer();
  }
}
