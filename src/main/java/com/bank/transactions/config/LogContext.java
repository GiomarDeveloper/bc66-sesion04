package com.bank.transactions.config;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Componente que permite propagar el contexto de correlación (Correlation ID)
 * dentro de los flujos reactivos (Mono y Flux) utilizando MDC/ThreadContext.
 */
@Component
@Log4j2
public class LogContext {

  private static final String CORR_ID = "corrId";

  /**
   * Asocia el contexto de correlación con un flujo {@link Mono}.
   *
   * @param mono flujo reactivo que se ejecutará dentro del contexto MDC.
   * @param <T>  tipo del elemento emitido.
   * @return flujo Mono con contexto MDC asociado.
   */
  public <T> Mono<T> withMdc(Mono<T> mono) {
    return Mono.deferContextual(ctx -> {
      if (ctx.hasKey(CORR_ID)) {
        String corr = ctx.get(CORR_ID).toString();
        ThreadContext.put(CORR_ID, corr);
        log.debug("MDC context set with correlationId: {}", corr);
      }
      return mono.doFinally(sig -> {
        ThreadContext.remove(CORR_ID);
        log.debug("MDC context cleared");
      });
    });
  }

  /**
   * Asocia el contexto de correlación con un flujo {@link Flux}.
   *
   * @param flux flujo reactivo que se ejecutará dentro del contexto MDC.
   * @param <T>  tipo del elemento emitido.
   * @return flujo Flux con contexto MDC asociado.
   */
  public <T> Flux<T> withMdc(Flux<T> flux) {
    return Flux.deferContextual(ctx -> {
      if (ctx.hasKey(CORR_ID)) {
        String corr = ctx.get(CORR_ID).toString();
        ThreadContext.put(CORR_ID, corr);
        log.debug("MDC context set with correlationId: {}", corr);
      }
      return flux.doFinally(sig -> {
        ThreadContext.remove(CORR_ID);
        log.debug("MDC context cleared");
      });
    });
  }
}