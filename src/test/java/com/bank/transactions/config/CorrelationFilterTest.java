package com.bank.transactions.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CorrelationFilterTest {

  private CorrelationFilter filter = new CorrelationFilter();

  @Test
  void filter_addsCorrelationHeader() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    WebFilterChain chain = webExchange -> {
      String correlationId = webExchange.getResponse().getHeaders().getFirst("X-Correlation-Id");
      assertNotNull(correlationId);
      return Mono.empty();
    };

    StepVerifier.create(filter.filter(exchange, chain))
      .verifyComplete();
  }
}