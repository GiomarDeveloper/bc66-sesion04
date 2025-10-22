package com.bank.transactions.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bank.transactions.config.LogContext;
import com.bank.transactions.domain.exception.BusinessException;
import com.bank.transactions.domain.model.Account;
import com.bank.transactions.domain.model.Transaction;
import com.bank.transactions.domain.repository.AccountRepository;
import com.bank.transactions.domain.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@WebFluxTest(TransactionService.class)
@Import({LogContext.class, SinksConfig.class}) // Importar configuraciones necesarias
class TransactionServiceCoverageTest {

  @Autowired
  private TransactionService transactionService;

  @MockBean
  private AccountRepository accountRepository;

  @MockBean
  private TransactionRepository transactionRepository;

  @MockBean
  private RiskRemoteClient riskRemoteClient;

  @MockBean
  private LogContext logContext;

  @Test
  void byAccount_accountNotFound() {
    when(accountRepository.findByNumber("nonexistent"))
      .thenReturn(Mono.empty());
    when(logContext.withMdc(any(Flux.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    StepVerifier.create(transactionService.byAccount("nonexistent"))
      .expectError(BusinessException.class)
      .verify();
  }

  @Test
  void byAccount_returnsTransactions() {
    Account account = new Account();
    account.setId(UUID.randomUUID().toString());
    account.setNumber("001-0001");

    Transaction transaction = Transaction.builder()
      .accountId(account.getId())
      .accountNumber("001-0001")
      .type("DEBIT")
      .amount(new BigDecimal("100"))
      .build();

    when(accountRepository.findByNumber("001-0001"))
      .thenReturn(Mono.just(account));
    when(transactionRepository.findByAccountIdOrderByTimestampDesc(account.getId()))
      .thenReturn(Flux.just(transaction));
    when(logContext.withMdc(any(Flux.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    StepVerifier.create(transactionService.byAccount("001-0001"))
      .expectNextCount(1)
      .verifyComplete();
  }
}
