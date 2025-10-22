package com.bank.transactions.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.transactions.application.dto.CreateTxRequest;
import com.bank.transactions.config.LogContext;
import com.bank.transactions.domain.exception.BusinessException;
import com.bank.transactions.domain.model.Account;
import com.bank.transactions.domain.model.Transaction;
import com.bank.transactions.domain.repository.AccountRepository;
import com.bank.transactions.domain.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@WebFluxTest(TransactionService.class)
@Import({LogContext.class, SinksConfig.class}) // Importar configuraciones necesarias
class TransactionServiceCreateTest {

  @Autowired
  private TransactionService transactionService;

  @MockBean
  private AccountRepository accountRepo; // Cambiado para que coincida con el nombre en el servicio

  @MockBean
  private TransactionRepository txRepo; // Cambiado para que coincida con el nombre en el servicio

  @MockBean
  private RiskRemoteClient riskRemoteClient;

  @MockBean
  private LogContext logContext;

  @MockBean
  private Sinks.Many<Transaction> txSink;

  private Account testAccount;
  private CreateTxRequest validRequest;

  @BeforeEach
  void setUp() {
    testAccount = new Account();
    testAccount.setId(UUID.randomUUID().toString());
    testAccount.setNumber("001-0001");
    testAccount.setBalance(new BigDecimal("1000"));
    testAccount.setCurrency("USD");

    validRequest = new CreateTxRequest();
    validRequest.setAccountNumber("001-0001");
    validRequest.setType("DEBIT");
    validRequest.setAmount(new BigDecimal("100"));
    validRequest.setCurrency("USD");

    when(logContext.withMdc(any(Mono.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));
    when(txSink.tryEmitNext(any(Transaction.class))).thenReturn(Sinks.EmitResult.OK);
  }

  @Test
  void create_successfulDebitTransaction() {
    // Configurar mocks - usando los nombres correctos que coinciden con el servicio
    when(accountRepo.findByNumber("001-0001"))
      .thenReturn(Mono.just(testAccount));
    when(riskRemoteClient.isAllowed("USD", "DEBIT", new BigDecimal("100")))
      .thenReturn(Mono.just(true));

    // Mock para la cuenta actualizada después del débito
    Account updatedAccount = new Account();
    updatedAccount.setId(testAccount.getId());
    updatedAccount.setNumber(testAccount.getNumber());
    updatedAccount.setBalance(new BigDecimal("900")); // 1000 - 100
    updatedAccount.setCurrency(testAccount.getCurrency());

    when(accountRepo.save(any(Account.class)))
      .thenReturn(Mono.just(updatedAccount));

    Transaction savedTransaction = Transaction.builder()
      .id(UUID.randomUUID().toString())
      .accountId(testAccount.getId())
      .accountNumber("001-0001")
      .type("DEBIT")
      .amount(new BigDecimal("100"))
      .currency("USD")
      .timestamp(Instant.now())
      .status("COMPLETED")
      .build();

    when(txRepo.save(any(Transaction.class)))
      .thenReturn(Mono.just(savedTransaction));

    // Ejecutar test
    StepVerifier.create(transactionService.create(validRequest))
      .expectNextMatches(tx ->
        "COMPLETED".equals(tx.getStatus()) &&
          "DEBIT".equals(tx.getType()) &&
          new BigDecimal("100").equals(tx.getAmount()))
      .verifyComplete();

    // Verificar interacciones - usando los nombres correctos
    verify(accountRepo).findByNumber("001-0001");
    verify(riskRemoteClient).isAllowed("USD", "DEBIT", new BigDecimal("100"));
    verify(accountRepo).save(any(Account.class));
    verify(txRepo).save(any(Transaction.class));
    verify(txSink).tryEmitNext(any(Transaction.class));
  }

  @Test
  void create_successfulCreditTransaction() {
    CreateTxRequest creditRequest = new CreateTxRequest();
    creditRequest.setAccountNumber("001-0001");
    creditRequest.setType("CREDIT");
    creditRequest.setAmount(new BigDecimal("200"));
    creditRequest.setCurrency("USD");

    when(accountRepo.findByNumber("001-0001"))
      .thenReturn(Mono.just(testAccount));
    when(riskRemoteClient.isAllowed("USD", "CREDIT", new BigDecimal("200")))
      .thenReturn(Mono.just(true));

    // Mock para la cuenta actualizada después del crédito
    Account updatedAccount = new Account();
    updatedAccount.setId(testAccount.getId());
    updatedAccount.setNumber(testAccount.getNumber());
    updatedAccount.setBalance(new BigDecimal("1200")); // 1000 + 200
    updatedAccount.setCurrency(testAccount.getCurrency());

    when(accountRepo.save(any(Account.class)))
      .thenReturn(Mono.just(updatedAccount));

    Transaction savedTransaction = Transaction.builder()
      .id(UUID.randomUUID().toString())
      .accountId(testAccount.getId())
      .accountNumber("001-0001")
      .type("CREDIT")
      .amount(new BigDecimal("200"))
      .currency("USD")
      .timestamp(Instant.now())
      .status("COMPLETED")
      .build();

    when(txRepo.save(any(Transaction.class)))
      .thenReturn(Mono.just(savedTransaction));

    StepVerifier.create(transactionService.create(creditRequest))
      .expectNextMatches(tx -> "CREDIT".equals(tx.getType()))
      .verifyComplete();
  }

  @Test
  void create_accountNotFound() {
    when(accountRepo.findByNumber("001-0001"))
      .thenReturn(Mono.empty());

    StepVerifier.create(transactionService.create(validRequest))
      .expectErrorMatches(throwable ->
        throwable instanceof BusinessException &&
          "account_not_found".equals(throwable.getMessage()))
      .verify();
  }

  @Test
  void create_invalidTransactionType() {
    CreateTxRequest invalidRequest = new CreateTxRequest();
    invalidRequest.setAccountNumber("001-0001");
    invalidRequest.setType("INVALID_TYPE");
    invalidRequest.setAmount(new BigDecimal("100"));
    invalidRequest.setCurrency("USD");

    when(accountRepo.findByNumber("001-0001"))
      .thenReturn(Mono.just(testAccount));

    StepVerifier.create(transactionService.create(invalidRequest))
      .expectErrorMatches(throwable ->
        throwable instanceof BusinessException &&
          throwable.getMessage().contains("invalid_transaction_type"))
      .verify();
  }

  @Test
  void create_riskRejected() {
    when(accountRepo.findByNumber("001-0001"))
      .thenReturn(Mono.just(testAccount));
    when(riskRemoteClient.isAllowed("USD", "DEBIT", new BigDecimal("100")))
      .thenReturn(Mono.just(false));

    StepVerifier.create(transactionService.create(validRequest))
      .expectErrorMatches(throwable ->
        throwable instanceof BusinessException &&
          "risk_rejected".equals(throwable.getMessage()))
      .verify();
  }

  @Test
  void create_insufficientFunds() {
    CreateTxRequest largeDebitRequest = new CreateTxRequest();
    largeDebitRequest.setAccountNumber("001-0001");
    largeDebitRequest.setType("DEBIT");
    largeDebitRequest.setAmount(new BigDecimal("2000")); // Más que el saldo
    largeDebitRequest.setCurrency("USD");

    when(accountRepo.findByNumber("001-0001"))
      .thenReturn(Mono.just(testAccount));
    when(riskRemoteClient.isAllowed("USD", "DEBIT", new BigDecimal("2000")))
      .thenReturn(Mono.just(true));

    StepVerifier.create(transactionService.create(largeDebitRequest))
      .expectErrorMatches(throwable ->
        throwable instanceof BusinessException &&
          "insufficient_funds".equals(throwable.getMessage()))
      .verify();
  }
}