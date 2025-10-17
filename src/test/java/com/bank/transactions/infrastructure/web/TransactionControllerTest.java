package com.bank.transactions.infrastructure.web;

import com.bank.transactions.application.dto.CreateTxRequest;
import com.bank.transactions.application.service.TransactionService;
import com.bank.transactions.domain.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TransactionService transactionService;

    @Test
    void createTransaction_Success() {
        // Given
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

        when(transactionService.create(any(CreateTxRequest.class)))
                .thenReturn(Mono.just(transaction));

        // When & Then
        webTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Transaction.class)
                .isEqualTo(transaction);
    }

    @Test
    void createTransaction_ValidationError() {
        // Given
        CreateTxRequest invalidRequest = new CreateTxRequest();
        // Missing required fields

        // When & Then
        webTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void listTransactions_Success() {
        // Given
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

        when(transactionService.byAccount("001-0001"))
                .thenReturn(Flux.just(tx1, tx2));

        // When & Then
        webTestClient.get()
                .uri("/api/transactions?accountNumber=001-0001")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Transaction.class)
                .hasSize(2)
                .contains(tx1, tx2);
    }

    @Test
    void streamTransactions_Success() {
        // Given
        Transaction transaction = Transaction.builder()
                .id("tx123")
                .accountId("acc123")
                .type("DEBIT")
                .amount(new BigDecimal("100"))
                .timestamp(Instant.now())
                .build();

        when(transactionService.stream())
                .thenReturn(Flux.empty()); // Para simplificar el test

        // When & Then
        webTestClient.get()
                .uri("/api/stream/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE);
    }
}
