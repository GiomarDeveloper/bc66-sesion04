package com.bank.transactions.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Representa una transacción bancaria.
 */
@Document("transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
  @Id
  private String id;

  private String accountId;

  private String accountNumber;

  private String currency;

  private String type;

  private BigDecimal amount;

  private Instant timestamp;

  private String status;

  private String reason;
}