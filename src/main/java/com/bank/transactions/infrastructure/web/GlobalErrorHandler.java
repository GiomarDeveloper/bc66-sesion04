package com.bank.transactions.infrastructure.web;

import com.bank.transactions.domain.exception.BusinessException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

/**
 * Controlador global de manejo de excepciones para los endpoints reactivos.
 * Intercepta excepciones de negocio, validación y errores genéricos,
 * devolviendo respuestas con un formato uniforme.
 */
@RestControllerAdvice
@Slf4j
public class GlobalErrorHandler {

  private static final String ERROR_KEY = "error";

  /**
   * Maneja excepciones del tipo {@link BusinessException}.
   *
   * @param ex excepción de negocio lanzada por la aplicación
   * @return respuesta con código HTTP 400 y mensaje de error
   */
  @ExceptionHandler(BusinessException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleBusinessException(BusinessException ex) {
    log.warn("Business exception: {}", ex.getMessage());
    return Mono.just(
      ResponseEntity.badRequest()
        .body(Map.of(ERROR_KEY, ex.getMessage()))
    );
  }

  /**
   * Maneja excepciones de validación lanzadas por {@link WebExchangeBindException}.
   *
   * @param ex excepción con los errores de validación
   * @return respuesta con código HTTP 400 y detalles de los campos inválidos
   */
  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(
      WebExchangeBindException ex) {
    log.warn("Validation exception: {}", ex.getMessage());
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
      .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    return Mono.just(
      ResponseEntity.badRequest()
        .body(Map.of(
          ERROR_KEY, "validation_failed",
          "details", errors
        ))
    );
  }

  /**
   * Maneja excepciones genéricas no controladas.
   *
   * @param ex excepción inesperada
   * @return respuesta con código HTTP 500 y mensaje genérico
   */
  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception ex) {
    log.error("Internal server error", ex);
    return Mono.just(
      ResponseEntity.internalServerError()
        .body(Map.of(ERROR_KEY, "internal_server_error"))
    );
  }
}
