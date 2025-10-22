package com.bank.transactions.config;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * Filtro WebFlux que agrega y propaga un identificador de correlación (Correlation ID)
 * en cada solicitud HTTP entrante y saliente.
 */
@Component
public class CorrelationFilter implements WebFilter {

  private static final String HEADER = "X-Correlation-Id";
  private static final String CORRELATION_ID_KEY = "corrId";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ServerHttpResponse response = exchange.getResponse();
    String correlationId = Optional.ofNullable(exchange.getRequest()
        .getHeaders()
        .getFirst(HEADER))
        .orElse(UUID.randomUUID().toString());

    response.getHeaders().add(HEADER, correlationId);

    return chain.filter(exchange)
      .contextWrite(Context.of(CORRELATION_ID_KEY, correlationId));
  }
}
