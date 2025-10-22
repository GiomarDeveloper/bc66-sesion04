package com.bank.transactions.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RiskRuleTest {

  @Test
  void riskRule_settersAndGetters() {
    RiskRule rule = new RiskRule();
    rule.setId(1L);
    rule.setCurrency("USD");
    rule.setMaxDebitPerTx(new BigDecimal("5000.00"));

    assertAll(
      () -> assertEquals(1L, rule.getId()),
      () -> assertEquals("USD", rule.getCurrency()),
      () -> assertEquals(new BigDecimal("5000.00"), rule.getMaxDebitPerTx())
    );
  }

  @Test
  void riskRule_builder() {
    RiskRule rule = RiskRule.builder()
      .id(2L)
      .currency("EUR")
      .maxDebitPerTx(new BigDecimal("10000.00"))
      .build();

    assertEquals(2L, rule.getId());
    assertEquals("EUR", rule.getCurrency());
    assertEquals(new BigDecimal("10000.00"), rule.getMaxDebitPerTx());
  }

  @Test
  void riskRule_equalsAndHashCode() {
    RiskRule r1 =
      RiskRule.builder().id(1L).currency("USD").maxDebitPerTx(new BigDecimal("5000.00")).build();
    RiskRule r2 =
      RiskRule.builder().id(1L).currency("USD").maxDebitPerTx(new BigDecimal("5000.00")).build();

    assertEquals(r1, r2);
    assertEquals(r1.hashCode(), r2.hashCode());
  }

  @Test
  void riskRule_equalsWithDifferentObjectOrNull() {
    RiskRule rule = RiskRule.builder().id(1L).currency("USD").build();
    assertNotEquals(null, rule);
    assertNotEquals(new Object(), rule);
  }

  @Test
  void riskRule_toString_containsClassName() {
    RiskRule rule = new RiskRule();
    assertTrue(rule.toString().contains("RiskRule"));
  }

  @Test
  void riskRule_defaultConstructor_notNull() {
    RiskRule rule = new RiskRule();

    assertNotNull(rule);

    assertNull(rule.getId());
    assertNull(rule.getCurrency());
    assertNull(rule.getMaxDebitPerTx());
  }
}
