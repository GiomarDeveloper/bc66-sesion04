package com.bank.transactions.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.bank.transactions.application.service.RiskService;
import com.bank.transactions.domain.model.RiskRule;
import com.bank.transactions.domain.repository.RiskRuleRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class SimpleCoverageTest {

  @Autowired
  private RiskService riskService;

  @MockBean
  private RiskRuleRepository riskRuleRepository;

  @Test
  void contextLoads() {
    assertNotNull(riskService);
  }

  @Test
  void riskService_creditAlwaysAllowed() {
    // Test simple: crédito siempre permitido
    Boolean result = riskService.isAllowedLegacy("USD", "CREDIT", new BigDecimal("1000"));
    assertTrue(result);
  }

  @Test
  void riskService_debitWithRule() {
    // Configurar regla simple
    RiskRule rule = new RiskRule();
    rule.setMaxDebitPerTx(new BigDecimal("500"));
    when(riskRuleRepository.findFirstByCurrency("USD")).thenReturn(Optional.of(rule));

    Boolean result = riskService.isAllowedLegacy("USD", "DEBIT", new BigDecimal("300"));
    assertTrue(result);
  }

  @Test
  void riskService_debitExceedsLimit() {
    // Configurar regla simple
    RiskRule rule = new RiskRule();
    rule.setMaxDebitPerTx(new BigDecimal("500"));
    when(riskRuleRepository.findFirstByCurrency("USD")).thenReturn(Optional.of(rule));

    Boolean result = riskService.isAllowedLegacy("USD", "DEBIT", new BigDecimal("600"));
    assertFalse(result);
  }
}