package com.bank.transactions.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * DTO para la creación de una transacción.
 * Contiene los datos necesarios para registrar una operación
 * financiera de una cuenta.
 */
@Data
public class CreateTxRequest {

  @NotBlank(message = "Account number is required")
  private String accountNumber;

  @NotBlank(message = "Transaction type is required")
  private String type;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  private String currency;
}
