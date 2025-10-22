package com.bank.transactions.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción personalizada para representar errores de negocio.
 * <p>
 * Esta excepción se lanza cuando se produce una violación de las reglas de negocio
 * y retorna una respuesta HTTP 400 (Bad Request).
 * </p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

  /**
   * Crea una nueva excepción de negocio con un mensaje descriptivo.
   *
   * @param message el mensaje que describe la causa del error.
   */
  public BusinessException(String message) {
    super(message);
  }

  /**
   * Crea una nueva excepción de negocio con un mensaje y una causa.
   *
   * @param message el mensaje que describe la causa del error.
   * @param cause   la excepción original que causó este error.
   */
  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }
}
