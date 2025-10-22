package com.bank.transactions.infrastructure.config;

import com.bank.transactions.domain.model.Account;
import com.bank.transactions.domain.model.RiskRule;
import com.bank.transactions.domain.repository.AccountRepository;
import com.bank.transactions.domain.repository.RiskRuleRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Inicializa datos de prueba en la base de datos al iniciar la aplicación.
 * Carga reglas de riesgo y cuentas predefinidas.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

  private final RiskRuleRepository riskRepo;
  private final AccountRepository accountRepo;

  /**
   * Ejecuta la carga de datos inicial al inicio del contexto de Spring Boot.
   *
   * @param args argumentos de línea de comando
   */
  @Override
  public void run(String... args) {
    log.info("Seeding initial data...");

    // Reglas de riesgo (bloqueante)
    riskRepo.deleteAllInBatch();
    riskRepo.save(
        RiskRule.builder()
          .currency("PEN")
          .maxDebitPerTx(new BigDecimal("1500"))
          .build()
    );
    riskRepo.save(
        RiskRule.builder()
          .currency("USD")
          .maxDebitPerTx(new BigDecimal("500"))
          .build()
    );
    log.info("Risk rules seeded");

    // Cuentas reactivas (Mongo)
    accountRepo.deleteAll()
      .thenMany(
        Flux.just(
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
        )
      )
      .flatMap(accountRepo::save)
      .doOnNext(acc -> log.info("Account seeded: {}", acc.getNumber()))
        .blockLast(); // Solo para seed en arranque
  }
}
