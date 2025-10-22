package com.bank.transactions.infrastructure.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(MockRiskController.class)
class MockRiskControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void allow_debitBelowThreshold_returnsTrue() {
    webTestClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/mock/risk/allow")
        .queryParam("currency", "USD")
        .queryParam("type", "DEBIT")
        .queryParam("amount", "1000") // Por debajo de 1200
        .build())
      .exchange()
      .expectStatus().isOk()
      .expectBody(Boolean.class)
      .value(Assertions::assertTrue);
  }

  @Test
  void allow_debitAboveThreshold_returnsFalse() {
    webTestClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/mock/risk/allow")
        .queryParam("currency", "USD")
        .queryParam("type", "DEBIT")
        .queryParam("amount", "1500") // Por encima de 1200
        .build())
      .exchange()
      .expectStatus().isOk()
      .expectBody(Boolean.class)
      .value(Assertions::assertFalse);
  }

  @Test
  void allow_creditAboveThreshold_returnsTrue() {
    webTestClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/mock/risk/allow")
        .queryParam("currency", "USD")
        .queryParam("type", "CREDIT")
        .queryParam("amount", "5000") // CREDIT no tiene límite
        .build())
      .exchange()
      .expectStatus().isOk()
      .expectBody(Boolean.class)
      .value(Assertions::assertTrue);
  }

  @Test
  void allow_withDelay_returnsTrue() {
    webTestClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/mock/risk/allow")
        .queryParam("currency", "USD")
        .queryParam("type", "DEBIT")
        .queryParam("amount", "100")
        .queryParam("delayMs", "50")
        .build())
      .exchange()
      .expectStatus().isOk()
      .expectBody(Boolean.class)
      .value(Assertions::assertTrue);
  }

  @Test
  void allow_forceFail_returnsError() {
    webTestClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/mock/risk/allow")
        .queryParam("currency", "USD")
        .queryParam("type", "DEBIT")
        .queryParam("amount", "100")
        .queryParam("fail", "true")
        .build())
      .exchange()
      .expectStatus().is5xxServerError();
  }

  @Test
  void allow_caseInsensitiveTypeCheck() {
    webTestClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/mock/risk/allow")
        .queryParam("currency", "USD")
        .queryParam("type", "debit") // minúsculas
        .queryParam("amount", "1500")
        .build())
      .exchange()
      .expectStatus().isOk()
      .expectBody(Boolean.class)
      .value(Assertions::assertFalse);
  }

  @Test
  void allow_differentCurrency_sameRules() {
    webTestClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/mock/risk/allow")
        .queryParam("currency", "EUR")
        .queryParam("type", "DEBIT")
        .queryParam("amount", "1500")
        .build())
      .exchange()
      .expectStatus().isOk()
      .expectBody(Boolean.class)
      .value(Assertions::assertFalse);
  }
}