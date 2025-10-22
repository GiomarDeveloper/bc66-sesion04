package com.bank.transactions.domain.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Representa una cuenta bancaria en el sistema.
 */
@Document("accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
  @Id
  private String id;

  private String number;

  private String holderName;

  private String currency;

  private BigDecimal balance;
}