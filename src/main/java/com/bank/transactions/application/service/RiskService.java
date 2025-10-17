package com.bank.transactions.application.service;

import com.bank.transactions.domain.model.RiskRule;
import com.bank.transactions.domain.repository.RiskRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    private final RiskRuleRepository riskRepo;

    public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {
        log.debug("Checking risk for currency: {}, type: {}, amount: {}", currency, type, amount);

        return Mono.fromCallable(() ->
                        riskRepo.findFirstByCurrency(currency)
                                .map(RiskRule::getMaxDebitPerTx)
                                .orElse(new BigDecimal("0")))
                .subscribeOn(Schedulers.boundedElastic()) // Aislar operación bloqueante
                .map(maxDebit -> {
                    if ("DEBIT".equalsIgnoreCase(type)) {
                        boolean allowed = amount.compareTo(maxDebit) <= 0;
                        log.debug("Debit transaction allowed: {} (amount: {}, max: {})", allowed, amount, maxDebit);
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
}