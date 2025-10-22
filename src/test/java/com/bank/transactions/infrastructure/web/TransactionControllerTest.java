package com.bank.transactions.infrastructure.web;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bank.transactions.application.dto.CreateTxRequest;
import com.bank.transactions.application.service.TransactionService;
import com.bank.transactions.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TransactionService transactionService;

    @Test
    void createTransaction_success() {
        CreateTxRequest request = new CreateTxRequest();
        request.setAccountNumber("001-0001");
        request.setType("DEBIT");
      request.setAmount(new BigDecimal("100.00"));
      request.setCurrency("USD");

      Transaction saved = Transaction.builder()
        .id(UUID.randomUUID().toString())
        .accountId("acc-001")
        .accountNumber("001-0001")
                .type("DEBIT")
        .amount(new BigDecimal("100.00"))
        .currency("USD")
                .timestamp(Instant.now())
        .status("COMPLETED")
                .build();

      when(transactionService.create(any(CreateTxRequest.class))).thenReturn(Mono.just(saved));

        webTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
          .expectBody()
          .jsonPath("$.id").isNotEmpty()
          .jsonPath("$.accountNumber").isEqualTo("001-0001")
          .jsonPath("$.status").isEqualTo("COMPLETED");
    }

    @Test
    void createTransaction_invalidRequest_returnsBadRequest() {
      CreateTxRequest invalid = new CreateTxRequest();
        webTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getTransactionsByAccount_success() {
      Transaction tx1 = Transaction.builder().id("tx1").accountNumber("001-0001").type("DEBIT")
        .amount(BigDecimal.TEN).currency("USD").status("OK").timestamp(Instant.now()).build();
      Transaction tx2 = Transaction.builder().id("tx2").accountNumber("001-0001").type("CREDIT")
        .amount(BigDecimal.ONE).currency("USD").status("OK").timestamp(Instant.now()).build();

      when(transactionService.byAccount("001-0001")).thenReturn(Flux.just(tx1, tx2));

        webTestClient.get()
                .uri("/api/transactions?accountNumber=001-0001")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Transaction.class)
          .hasSize(2);
    }

    @Test
    void getTransactionsByAccount_emptyList() {
      when(transactionService.byAccount("001-0001")).thenReturn(Flux.empty());

        webTestClient.get()
          .uri("/api/transactions?accountNumber=001-0001")
                .exchange()
                .expectStatus().isOk()
          .expectBodyList(Transaction.class)
          .hasSize(0);
    }
}