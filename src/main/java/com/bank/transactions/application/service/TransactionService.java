package com.bank.transactions.application.service;

import com.bank.transactions.application.dto.CreateTxRequest;
import com.bank.transactions.config.LogContext;
import com.bank.transactions.domain.exception.BusinessException;
import com.bank.transactions.domain.model.Account;
import com.bank.transactions.domain.model.Transaction;
import com.bank.transactions.domain.repository.AccountRepository;
import com.bank.transactions.domain.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

/**
 * Servicio principal encargado de manejar las operaciones relacionadas con
 * transacciones bancarias.
 *
 * <p>Incluye la creación de transacciones, validaciones de riesgo,
 * actualizaciones de saldo y emisión de eventos reactivos.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

  private static final String DEBIT_KEY = "DEBIT";
  private final AccountRepository accountRepo;
  private final TransactionRepository txRepo;
  private final RiskRemoteClient riskRemoteClient;
  private final LogContext logContext;
  private final Sinks.Many<Transaction> txSink;

  /**
   * Crea una nueva transacción validando tipo, fondos y reglas de riesgo.
   *
   * @param req la solicitud de creación de transacción
   * @return un {@link Mono} con la transacción creada
   */
  @Transactional
  public Mono<Transaction> create(CreateTxRequest req) {
    log.info("Creating transaction: {}", req);

    return logContext.withMdc(
      accountRepo.findByNumber(req.getAccountNumber())
        .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
        .flatMap(acc -> validateAndApply(acc, req))
        .doOnSuccess(tx -> {
          log.info("Transaction created successfully: {}", tx.getId());
          txSink.tryEmitNext(tx);
        })
        .doOnError(error -> log.error("Error creating transaction: {}", error.getMessage()))
    );
  }

  /**
   * Valida los datos de la transacción y aplica el cambio en el balance.
   *
   * @param acc la cuenta asociada
   * @param req los datos de la transacción
   * @return un {@link Mono} con la transacción persistida
   */
  private Mono<Transaction> validateAndApply(Account acc, CreateTxRequest req) {
    String type = req.getType().toUpperCase();
    BigDecimal amount = req.getAmount();

    if (!"CREDIT".equals(type) && !DEBIT_KEY.equals(type)) {
      return Mono.error(new BusinessException("invalid_transaction_type"));
    }

    // Validación de riesgo remoto
    return riskRemoteClient.isAllowed(acc.getCurrency(), type, amount)
      .flatMap(allowed -> {
        if (!allowed) {
          return Mono.error(new BusinessException("risk_rejected"));
        }

        // Validación de fondos
        if (DEBIT_KEY.equals(type) && acc.getBalance().compareTo(amount) < 0) {
          return Mono.error(new BusinessException("insufficient_funds"));
        }

        // Actualizar balance y registrar transacción
        return Mono.just(acc)
          .publishOn(Schedulers.parallel())
          .map(account -> {
            BigDecimal newBalance = DEBIT_KEY.equals(type)
                ? account.getBalance().subtract(amount)
                : account.getBalance().add(amount);
            account.setBalance(newBalance);
            return account;
          })
          .flatMap(accountRepo::save)
          .flatMap(updatedAccount -> txRepo.save(Transaction.builder()
            .accountId(updatedAccount.getId())
            .accountNumber(updatedAccount.getNumber())
            .type(type)
            .amount(amount)
            .currency(acc.getCurrency())
            .timestamp(Instant.now())
            .status("COMPLETED")
            .build()));
      });
  }

  /**
   * Recupera todas las transacciones de una cuenta específica, ordenadas por fecha descendente.
   *
   * @param accountNumber número de cuenta
   * @return un {@link Flux} con las transacciones de la cuenta
   */
  public Flux<Transaction> byAccount(String accountNumber) {
    log.debug("Fetching transactions for account: {}", accountNumber);

    return logContext.withMdc(
      accountRepo.findByNumber(accountNumber)
        .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
        .flatMapMany(acc -> txRepo.findByAccountIdOrderByTimestampDesc(acc.getId()))
        .doOnComplete(() ->
          log.debug("Completed fetching transactions for account: {}", accountNumber))
    );
  }

  /**
   * Devuelve un flujo reactivo (Server-Sent Events) con las transacciones en tiempo real.
   *
   * @return un {@link Flux} de {@link ServerSentEvent} con transacciones
   */
  public Flux<ServerSentEvent<Transaction>> stream() {
    return txSink.asFlux()
      .map(transaction -> ServerSentEvent.builder(transaction)
        .event("transaction")
        .build())
      .doOnSubscribe(subscription -> log.debug("New subscriber to transaction stream"))
      .doOnCancel(() -> log.debug("Transaction stream subscription cancelled"));
  }
}
