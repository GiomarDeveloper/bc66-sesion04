package com.bank.transactions.infrastructure.web;

import java.math.BigDecimal;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controlador mock que simula un servicio remoto de evaluación de riesgo.
 * Se utiliza principalmente para pruebas locales o de integración.
 */
@RestController
@RequestMapping("/mock/risk")
@Slf4j
public class MockRiskController {

  /**
   * Endpoint que simula la respuesta del servicio de riesgo.
   *
   * @param currency tipo de moneda (por ejemplo, "USD")
   * @param type     tipo de transacción (DEBIT o CREDIT)
   * @param amount   monto de la transacción
   * @param fail     si es true, simula un error del servicio
   * @param delayMs  retraso artificial en milisegundos para simular latencia
   * @return {@link Mono} que emite true si la operación está permitida; false si está rechazada
   */
  @GetMapping("/allow")
  public Mono<Boolean> allow(
      @RequestParam String currency,
      @RequestParam String type,
      @RequestParam BigDecimal amount,
      @RequestParam(defaultValue = "false") boolean fail,
      @RequestParam(defaultValue = "0") long delayMs) {

    log.info(
        "Risk check - currency: {}, type: {}, amount: {}, fail: {}, delay: {}ms",
        currency, type, amount, fail, delayMs);

    if (fail) {
      log.warn("Simulating risk service failure");
      return Mono.error(new RuntimeException("risk_service_unavailable"));
    }

    boolean allowed = !("DEBIT".equalsIgnoreCase(type)
        && amount.compareTo(new BigDecimal("1200")) > 0);

    log.info("Risk decision: {}", allowed);

    return Mono.just(allowed)
      .delayElement(Duration.ofMillis(delayMs))
      .doOnNext(result -> log.debug("Risk check completed: {}", result));
  }
}
