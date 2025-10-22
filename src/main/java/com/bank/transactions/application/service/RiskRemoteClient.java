package com.bank.transactions.application.service;

import com.bank.transactions.config.LogContext;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Cliente remoto para verificar riesgos utilizando WebClient y Resilience4j.
 * Incluye mecanismos de tolerancia a fallos y fallback a un servicio legado.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskRemoteClient {

  private final WebClient riskWebClient;
  private final LogContext logContext;
  private final RiskService legacyRiskService;

  /**
   * Verifica si una operación está permitida según el servicio de riesgo remoto.
   *
   * @param currency tipo de moneda
   * @param type     tipo de operación
   * @param amount   monto de la operación
   * @return Mono con valor booleano que indica si está permitido
   */
  @TimeLimiter(name = "riskClient")
  @Retry(name = "riskClient")
  @CircuitBreaker(name = "riskClient", fallbackMethod = "fallback")
  public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {
    log.debug("Calling remote risk service - currency: {}, type: {}, amount: {}",
        currency, type, amount);

    return riskWebClient.get()
      .uri(uri -> uri.path("/allow")
        .queryParam("currency", currency)
        .queryParam("type", type)
        .queryParam("amount", amount)
        .queryParam("fail", false)
        .queryParam("delayMs", 200)
        .build())
      .retrieve()
      .bodyToMono(Boolean.class)
      .doOnNext(result -> log.debug("Remote risk service returned: {}", result))
      .doOnError(error -> log.error("Error calling remote risk service: {}", error.getMessage()));
  }

  /**
   * Método fallback que se usa cuando el servicio remoto falla.
   *
   * @param currency tipo de moneda
   * @param type     tipo de operación
   * @param amount   monto de la operación
   * @param ex       excepción ocurrida durante la llamada
   * @return resultado del servicio de riesgo legado
   */
  public Mono<Boolean> fallback(String currency, String type, BigDecimal amount, Throwable ex) {
    log.warn("Using fallback to legacy risk service due to: {}", ex.getMessage());
    return legacyAllowed(currency, type, amount);
  }

  /**
   * Método auxiliar para invocar el módulo de riesgo legado de forma reactiva.
   *
   * @param currency tipo de moneda
   * @param type     tipo de operación
   * @param amount   monto de la operación
   * @return Mono con el resultado del módulo legado
   */
  private Mono<Boolean> legacyAllowed(String currency, String type, BigDecimal amount) {
    return Mono.fromCallable(() -> legacyRiskService.isAllowedLegacy(currency, type, amount))
      .doOnNext(result -> log.info("Legacy risk service returned: {}", result))
      .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
  }
}
