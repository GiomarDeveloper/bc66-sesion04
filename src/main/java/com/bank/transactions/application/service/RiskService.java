package com.bank.transactions.application.service;

import com.bank.transactions.domain.model.RiskRule;
import com.bank.transactions.domain.repository.RiskRuleRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Servicio encargado de evaluar las reglas de riesgo asociadas a las transacciones.
 * Determina si una transacción (crédito o débito) está permitida según las políticas configuradas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

  private final RiskRuleRepository riskRepo;

  /**
   * Verifica de manera reactiva si una transacción está permitida según la moneda,
   * el tipo (CREDIT o DEBIT) y el monto especificado.
   *
   * @param currency  moneda de la transacción
   * @param type      tipo de transacción (CREDIT o DEBIT)
   * @param amount    monto de la transacción
   * @return          un {@link Mono} que emite {@code true} si la transacción está permitida,
   *                  o {@code false} en caso contrario
   */
  public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {
    log.debug("Checking risk for currency: {}, type: {}, amount: {}", currency, type, amount);

    return Mono.fromCallable(() ->
        riskRepo.findFirstByCurrency(currency)
          .map(RiskRule::getMaxDebitPerTx)
          .orElse(BigDecimal.ZERO))
      .subscribeOn(Schedulers.boundedElastic())
      .map(maxDebit -> {
        if ("DEBIT".equalsIgnoreCase(type)) {
          boolean allowed = amount.compareTo(maxDebit) <= 0;
          log.debug(
              "Debit transaction allowed: {} (amount: {}, max: {})",
              allowed, amount, maxDebit
          );
          return allowed;
        }
        log.debug("Credit transaction always allowed");
        return true;
      })
      .onErrorResume(throwable -> {
        log.error("Error checking risk rules", throwable);
        return Mono.just(false);
      });
  }

  /**
   * Verificación alternativa no reactiva de las reglas de riesgo.
   * Se utiliza como mecanismo de respaldo en casos donde no se requiera flujo reactivo.
   *
   * @param currency moneda de la transacción
   * @param type     tipo de transacción (CREDIT o DEBIT)
   * @param amount   monto de la transacción
   * @return {@code true} si la transacción está permitida, {@code false} en caso contrario
   */
  public Boolean isAllowedLegacy(String currency, String type, BigDecimal amount) {
    log.debug(
        "Using legacy risk check for currency: {}, type: {}, amount: {}",
        currency, type, amount
    );
    try {
      RiskRule rule = riskRepo.findFirstByCurrency(currency)
          .orElse(new RiskRule());

      BigDecimal maxDebit = rule.getMaxDebitPerTx() != null
          ? rule.getMaxDebitPerTx()
          : new BigDecimal("1000");

      if ("DEBIT".equalsIgnoreCase(type)) {
        boolean allowed = amount.compareTo(maxDebit) <= 0;
        log.debug(
            "Legacy debit check - allowed: {}, amount: {}, max: {}",
            allowed, amount, maxDebit
        );
        return allowed;
      }
      return true;
    } catch (Exception e) {
      log.error("Error in legacy risk check", e);
      return false;
    }
  }
}
