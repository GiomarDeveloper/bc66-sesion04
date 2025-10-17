package com.bank.transactions.application.service;

import com.bank.transactions.application.dto.CreateTxRequest;
import com.bank.transactions.domain.exception.BusinessException;
import com.bank.transactions.domain.model.Account;
import com.bank.transactions.domain.model.Transaction;
import com.bank.transactions.domain.repository.AccountRepository;
import com.bank.transactions.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RiskService riskService;

    @Mock
    private Sinks.Many<Transaction> txSink;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_Success() {
        // Given
        Account account = Account.builder()
                .id("acc123")
                .number("001-0001")
                .balance(new BigDecimal("2000"))
                .currency("PEN")
                .build();

        CreateTxRequest request = new CreateTxRequest();
        request.setAccountNumber("001-0001");
        request.setType("DEBIT");
        request.setAmount(new BigDecimal("100"));

        Transaction transaction = Transaction.builder()
                .id("tx123")
                .accountId("acc123")
                .type("DEBIT")
                .amount(new BigDecimal("100"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        when(accountRepository.findByNumber("001-0001")).thenReturn(Mono.just(account));
        when(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("100"))).thenReturn(Mono.just(true));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(account));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(Mono.just(transaction));
        when(txSink.tryEmitNext(any(Transaction.class))).thenReturn(Sinks.EmitResult.OK);

        // When & Then
        StepVerifier.create(transactionService.create(request))
                .expectNext(transaction)
                .verifyComplete();
    }

    @Test
    void createTransaction_AccountNotFound() {
        // Given
        CreateTxRequest request = new CreateTxRequest();
        request.setAccountNumber("999-9999");
        request.setType("DEBIT");
        request.setAmount(new BigDecimal("100"));

        when(accountRepository.findByNumber("999-9999")).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(transactionService.create(request))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void createTransaction_RiskRejected() {
        // Given
        Account account = Account.builder()
                .id("acc123")
                .number("001-0001")
                .balance(new BigDecimal("2000"))
                .currency("PEN")
                .build();

        CreateTxRequest request = new CreateTxRequest();
        request.setAccountNumber("001-0001");
        request.setType("DEBIT");
        request.setAmount(new BigDecimal("2000"));

        when(accountRepository.findByNumber("001-0001")).thenReturn(Mono.just(account));
        when(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("2000"))).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(transactionService.create(request))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                "risk_rejected".equals(throwable.getMessage()))
                .verify();
    }

    @Test
    void createTransaction_InsufficientFunds() {
        // Given
        Account account = Account.builder()
                .id("acc123")
                .number("001-0002")
                .balance(new BigDecimal("800"))
                .currency("PEN")
                .build();

        CreateTxRequest request = new CreateTxRequest();
        request.setAccountNumber("001-0002");
        request.setType("DEBIT");
        request.setAmount(new BigDecimal("1000"));

        when(accountRepository.findByNumber("001-0002")).thenReturn(Mono.just(account));
        when(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("1000"))).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(transactionService.create(request))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                "insufficient_funds".equals(throwable.getMessage()))
                .verify();
    }

    @Test
    void getTransactionsByAccount() {
        // Given
        Account account = Account.builder()
                .id("acc123")
                .number("001-0001")
                .build();

        Transaction tx1 = Transaction.builder()
                .id("tx1")
                .accountId("acc123")
                .type("DEBIT")
                .amount(new BigDecimal("100"))
                .timestamp(Instant.now())
                .build();

        Transaction tx2 = Transaction.builder()
                .id("tx2")
                .accountId("acc123")
                .type("CREDIT")
                .amount(new BigDecimal("500"))
                .timestamp(Instant.now())
                .build();

        when(accountRepository.findByNumber("001-0001")).thenReturn(Mono.just(account));
        when(transactionRepository.findByAccountIdOrderByTimestampDesc("acc123"))
                .thenReturn(Flux.just(tx1, tx2));

        // When & Then
        StepVerifier.create(transactionService.byAccount("001-0001"))
                .expectNext(tx1)
                .expectNext(tx2)
                .verifyComplete();
    }

    @Test
    void getTransactionsByAccount_NotFound() {
        // Given
        when(accountRepository.findByNumber("999-9999")).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(transactionService.byAccount("999-9999"))
                .expectError(BusinessException.class)
                .verify();
    }
}
