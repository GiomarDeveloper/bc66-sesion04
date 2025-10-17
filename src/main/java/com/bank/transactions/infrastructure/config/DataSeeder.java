package com.bank.transactions.infrastructure.config;

import com.bank.transactions.domain.model.Account;
import com.bank.transactions.domain.model.RiskRule;
import com.bank.transactions.domain.repository.AccountRepository;
import com.bank.transactions.domain.repository.RiskRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RiskRuleRepository riskRepo;
    private final AccountRepository accountRepo;

    @Override
    public void run(String... args) {
        log.info("Seeding initial data...");

        // Bloqueante (JPA) - Risk Rules
        riskRepo.deleteAllInBatch();
        riskRepo.save(RiskRule.builder().currency("PEN").maxDebitPerTx(new BigDecimal("1500")).build());
        riskRepo.save(RiskRule.builder().currency("USD").maxDebitPerTx(new BigDecimal("500")).build());
        log.info("Risk rules seeded");

        // Reactivo (Mongo) - Accounts
        accountRepo.deleteAll()
                .thenMany(Flux.just(
                        Account.builder()
                                .number("001-0001")
                                .holderName("Ana Peru")
                                .currency("PEN")
                                .balance(new BigDecimal("2000"))
                                .build(),
                        Account.builder()
                                .number("001-0002")
                                .holderName("Luis Acuña")
                                .currency("PEN")
                                .balance(new BigDecimal("800"))
                                .build(),
                        Account.builder()
                                .number("001-0003")
                                .holderName("Carlos Dollar")
                                .currency("USD")
                                .balance(new BigDecimal("1000"))
                                .build()
                ))
                .flatMap(accountRepo::save)
                .doOnNext(acc -> log.info("Account seeded: {}", acc.getNumber()))
                .blockLast(); // Solo para seed en arranque
    }
}