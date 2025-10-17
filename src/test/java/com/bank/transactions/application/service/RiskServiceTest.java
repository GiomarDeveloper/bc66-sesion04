package com.bank.transactions.application.service;

import com.bank.transactions.domain.model.RiskRule;
import com.bank.transactions.domain.repository.RiskRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskServiceTest {

    @Mock
    private RiskRuleRepository riskRuleRepository;

    @InjectMocks
    private RiskService riskService;

    @Test
    void allowsDebitUnderLimit() {
        // Given
        RiskRule riskRule = RiskRule.builder()
                .currency("PEN")
                .maxDebitPerTx(new BigDecimal("1500"))
                .build();

        when(riskRuleRepository.findFirstByCurrency("PEN"))
                .thenReturn(Optional.of(riskRule));

        // When & Then
        StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("100")))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void rejectsDebitOverLimit() {
        // Given
        RiskRule riskRule = RiskRule.builder()
                .currency("PEN")
                .maxDebitPerTx(new BigDecimal("1500"))
                .build();

        when(riskRuleRepository.findFirstByCurrency("PEN"))
                .thenReturn(Optional.of(riskRule));

        // When & Then
        StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("2000")))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void allowsCreditTransaction() {
        // Given
        when(riskRuleRepository.findFirstByCurrency(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        StepVerifier.create(riskService.isAllowed("PEN", "CREDIT", new BigDecimal("10000")))
                .expectNext(true)
                .verifyComplete();
    }
}
