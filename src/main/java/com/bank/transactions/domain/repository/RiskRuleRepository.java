package com.bank.transactions.domain.repository;

import com.bank.transactions.domain.model.RiskRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para las reglas de riesgo de transacciones.
 */
public interface RiskRuleRepository extends JpaRepository<RiskRule, Long> {

  /**
   * Obtiene la primera regla de riesgo registrada para una moneda dada.
   *
   * @param currency tipo de moneda (ej. "PEN", "USD")
   * @return {@link Optional} con la regla de riesgo, si existe
   */
  Optional<RiskRule> findFirstByCurrency(String currency);
}
