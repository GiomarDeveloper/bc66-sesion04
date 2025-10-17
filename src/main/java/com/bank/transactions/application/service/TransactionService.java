package com.bank.transactions.application.service;

import com.bank.transactions.application.dto.CreateTxRequest;
import com.bank.transactions.domain.exception.BusinessException;
import com.bank.transactions.domain.model.Account;
import com.bank.transactions.domain.model.Transaction;
import com.bank.transactions.domain.repository.AccountRepository;
import com.bank.transactions.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final RiskService riskService;
    private final Sinks.Many<Transaction> txSink;

    @Transactional
    public Mono<Transaction> create(CreateTxRequest req) {
        log.info("Creating transaction: {}", req);

        return accountRepo.findByNumber(req.getAccountNumber())
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMap(acc -> validateAndApply(acc, req))
                .doOnSuccess(tx -> log.info("Transaction created successfully: {}", tx.getId()))
                .doOnError(error -> log.error("Error creating transaction: {}", error.getMessage()));
    }

    private Mono<Transaction> validateAndApply(Account acc, CreateTxRequest req) {
        String type = req.getType().toUpperCase();
        BigDecimal amount = req.getAmount();

        if (!"CREDIT".equals(type) && !"DEBIT".equals(type)) {
            return Mono.error(new BusinessException("invalid_transaction_type"));
        }

        // 1) Validación de riesgo (bloqueante envuelto -> elastic)
        return riskService.isAllowed(acc.getCurrency(), type, amount)
                .flatMap(allowed -> {
                    if (!allowed) {
                        return Mono.error(new BusinessException("risk_rejected"));
                    }

                    // 2) Validación de fondos
                    if ("DEBIT".equals(type) && acc.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new BusinessException("insufficient_funds"));
                    }

                    // 3) Actualizar balance
                    return Mono.just(acc)
                            .publishOn(Schedulers.parallel())
                            .map(account -> {
                                BigDecimal newBalance = "DEBIT".equals(type)
                                        ? account.getBalance().subtract(amount)
                                        : account.getBalance().add(amount);
                                account.setBalance(newBalance);
                                return account;
                            })
                            .flatMap(accountRepo::save)

                            // 4) Persistir transacción
                            .flatMap(updatedAccount -> txRepo.save(Transaction.builder()
                                    .accountId(updatedAccount.getId())
                                    .type(type)
                                    .amount(amount)
                                    .timestamp(Instant.now())
                                    .status("OK")
                                    .build()))

                            // 5) Notificar por SSE
                            .doOnNext(transaction -> {
                                log.debug("Emitting transaction to SSE: {}", transaction.getId());
                                txSink.tryEmitNext(transaction);
                            });
                });
    }

    public Flux<Transaction> byAccount(String accountNumber) {
        log.debug("Fetching transactions for account: {}", accountNumber);

        return accountRepo.findByNumber(accountNumber)
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMapMany(acc -> txRepo.findByAccountIdOrderByTimestampDesc(acc.getId()))
                .doOnComplete(() -> log.debug("Completed fetching transactions for account: {}", accountNumber));
    }

    public Flux<ServerSentEvent<Transaction>> stream() {
        return txSink.asFlux()
                .map(transaction -> ServerSentEvent.builder(transaction)
                        .event("transaction")
                        .build())
                .doOnSubscribe(subscription -> log.debug("New subscriber to transaction stream"))
                .doOnCancel(() -> log.debug("Transaction stream subscription cancelled"));
    }
}
