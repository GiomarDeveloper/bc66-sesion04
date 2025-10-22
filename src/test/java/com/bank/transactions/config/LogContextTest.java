package com.bank.transactions.config;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class LogContextTest {

  private final LogContext logContext = new LogContext();

  @AfterEach
  void cleanUp() {
    // Limpia el contexto del hilo después de cada test
    ThreadContext.clearAll();
  }

  @Test
  void withMdc_Mono_shouldPropagateValueAndSetMdc() {
    Mono<String> mono = Mono.just("test")
      .contextWrite(ctx -> ctx.put("corrId", "12345"));

    Mono<String> result = logContext.withMdc(mono);

    StepVerifier.create(result)
      .expectNext("test")
      .verifyComplete();

    // Verificar que el MDC fue limpiado
    assertNull(ThreadContext.get("corrId"));
  }

  @Test
  void withMdc_Mono_withoutContext_shouldNotFail() {
    Mono<String> mono = Mono.just("no-context");

    Mono<String> result = logContext.withMdc(mono);

    StepVerifier.create(result)
      .expectNext("no-context")
      .verifyComplete();

    assertNull(ThreadContext.get("corrId"));
  }

  @Test
  void withMdc_Flux_shouldPropagateValuesAndSetMdc() {
    Flux<String> flux = Flux.just("A", "B", "C")
      .contextWrite(ctx -> ctx.put("corrId", "XYZ"));

    Flux<String> result = logContext.withMdc(flux);

    StepVerifier.create(result)
      .expectNext("A", "B", "C")
      .verifyComplete();

    assertNull(ThreadContext.get("corrId"));
  }

  @Test
  void withMdc_Flux_withoutContext_shouldNotFail() {
    Flux<String> flux = Flux.just("1", "2", "3");

    Flux<String> result = logContext.withMdc(flux);

    StepVerifier.create(result)
      .expectNext("1", "2", "3")
      .verifyComplete();

    assertNull(ThreadContext.get("corrId"));
  }
}
