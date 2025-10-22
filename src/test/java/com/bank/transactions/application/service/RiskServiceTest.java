package com.bank.transactions.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import com.bank.transactions.domain.model.RiskRule;
import com.bank.transactions.domain.repository.RiskRuleRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RiskServiceTest {

  private RiskRuleRepository riskRepo;
    private RiskService riskService;

  @BeforeEach
  void setup() {
    riskRepo = Mockito.mock(RiskRuleRepository.class);
    riskService = new RiskService(riskRepo);
  }

  @Test
  void isAllowed_debitBelowLimit_shouldReturnTrue() {
    RiskRule rule =
      RiskRule.builder().currency("USD").maxDebitPerTx(new BigDecimal("5000")).build();
    when(riskRepo.findFirstByCurrency("USD")).thenReturn(Optional.of(rule));

    Mono<Boolean> result = riskService.isAllowed("USD", "DEBIT", new BigDecimal("1000"));

    StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isAllowed_debitAboveLimit_shouldReturnFalse() {
      RiskRule rule =
        RiskRule.builder().currency("USD").maxDebitPerTx(new BigDecimal("1000")).build();
      when(riskRepo.findFirstByCurrency("USD")).thenReturn(Optional.of(rule));

      StepVerifier.create(riskService.isAllowed("USD", "DEBIT", new BigDecimal("5000")))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isAllowed_creditTransaction_shouldAlwaysReturnTrue() {
      when(riskRepo.findFirstByCurrency("USD")).thenReturn(Optional.empty());

      StepVerifier.create(riskService.isAllowed("USD", "CREDIT", new BigDecimal("5000")))
                .expectNext(true)
                .verifyComplete();
    }

  @Test
  void isAllowed_onError_shouldReturnFalse() {
    when(riskRepo.findFirstByCurrency(anyString())).thenThrow(new RuntimeException("DB error"));

    StepVerifier.create(riskService.isAllowed("USD", "DEBIT", BigDecimal.TEN))
      .expectNext(false)
      .verifyComplete();
  }

  @Test
  void isAllowedLegacy_debitBelowLimit_shouldReturnTrue() {
    RiskRule rule =
      RiskRule.builder().currency("USD").maxDebitPerTx(new BigDecimal("2000")).build();
    when(riskRepo.findFirstByCurrency("USD")).thenReturn(Optional.of(rule));

    Boolean allowed = riskService.isAllowedLegacy("USD", "DEBIT", new BigDecimal("1000"));
    assertTrue(allowed);
  }

  @Test
  void isAllowedLegacy_debitAboveLimit_shouldReturnFalse() {
    RiskRule rule =
      RiskRule.builder().currency("USD").maxDebitPerTx(new BigDecimal("1000")).build();
    when(riskRepo.findFirstByCurrency("USD")).thenReturn(Optional.of(rule));

    Boolean allowed = riskService.isAllowedLegacy("USD", "DEBIT", new BigDecimal("5000"));
    assertFalse(allowed);
  }

  @Test
  void isAllowedLegacy_creditAlwaysAllowed() {
    when(riskRepo.findFirstByCurrency("USD")).thenReturn(Optional.empty());

    Boolean allowed = riskService.isAllowedLegacy("USD", "CREDIT", new BigDecimal("5000"));
    assertTrue(allowed);
  }

  @Test
  void isAllowedLegacy_exceptionHandled_shouldReturnFalse() {
    when(riskRepo.findFirstByCurrency(anyString())).thenThrow(new RuntimeException("DB error"));

    Boolean allowed = riskService.isAllowedLegacy("USD", "DEBIT", new BigDecimal("1000"));
    assertFalse(allowed);
  }
}
