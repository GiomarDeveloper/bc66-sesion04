package com.bank.transactions.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TransactionTest {

  @Test
  void transaction_builderWorks() {
    Instant now = Instant.now();
    Transaction transaction = Transaction.builder()
      .id("tx-123")
      .accountId("acc-456")
      .accountNumber("001-0001")
      .currency("USD")
      .type("DEBIT")
      .amount(new BigDecimal("100.50"))
      .timestamp(now)
      .status("COMPLETED")
      .reason("Payment OK")
      .build();

    assertAll(
      () -> assertEquals("tx-123", transaction.getId()),
      () -> assertEquals("acc-456", transaction.getAccountId()),
      () -> assertEquals("001-0001", transaction.getAccountNumber()),
      () -> assertEquals("USD", transaction.getCurrency()),
      () -> assertEquals("DEBIT", transaction.getType()),
      () -> assertEquals(new BigDecimal("100.50"), transaction.getAmount()),
      () -> assertEquals(now, transaction.getTimestamp()),
      () -> assertEquals("COMPLETED", transaction.getStatus()),
      () -> assertEquals("Payment OK", transaction.getReason())
    );
  }

  @Test
  void transaction_settersAndGetters() {
    Transaction transaction = new Transaction();
    Instant now = Instant.now();

    transaction.setId("tx-999");
    transaction.setAccountId("acc-888");
    transaction.setAccountNumber("001-0002");
    transaction.setCurrency("EUR");
    transaction.setType("CREDIT");
    transaction.setAmount(new BigDecimal("200.75"));
    transaction.setTimestamp(now);
    transaction.setStatus("PENDING");
    transaction.setReason("Waiting approval");

    assertAll(
      () -> assertEquals("tx-999", transaction.getId()),
      () -> assertEquals("acc-888", transaction.getAccountId()),
      () -> assertEquals("001-0002", transaction.getAccountNumber()),
      () -> assertEquals("EUR", transaction.getCurrency()),
      () -> assertEquals("CREDIT", transaction.getType()),
      () -> assertEquals(new BigDecimal("200.75"), transaction.getAmount()),
      () -> assertEquals(now, transaction.getTimestamp()),
      () -> assertEquals("PENDING", transaction.getStatus()),
      () -> assertEquals("Waiting approval", transaction.getReason())
    );
  }

  @Test
  void transaction_equalsAndHashCode_sameValues_shouldBeEqual() {
    Instant now = Instant.now();
    Transaction t1 = Transaction.builder()
      .id("t1").accountId("a1").accountNumber("001")
      .currency("USD").type("DEBIT").amount(new BigDecimal("10.00"))
      .timestamp(now).status("OK").reason("Approved").build();

    Transaction t2 = Transaction.builder()
      .id("t1").accountId("a1").accountNumber("001")
      .currency("USD").type("DEBIT").amount(new BigDecimal("10.00"))
      .timestamp(now).status("OK").reason("Approved").build();

    assertEquals(t1, t2);
    assertEquals(t1.hashCode(), t2.hashCode());
  }

  @Test
  void transaction_equals_differentValues_shouldNotBeEqual() {
    Transaction t1 = Transaction.builder().id("A").build();
    Transaction t2 = Transaction.builder().id("B").build();
    assertNotEquals(t1, t2);
  }

  @Test
  void transaction_equalsWithDifferentObjectOrNull() {
    Transaction tx = Transaction.builder().id("t1").build();
    assertNotEquals(null, tx);
    assertNotEquals(new Object(), tx);
  }

  @Test
  void transaction_canEqual_shouldReturnTrueForSameType() {
    Transaction tx1 = new Transaction();
    Transaction tx2 = new Transaction();
    assertTrue(tx1.canEqual(tx2));
  }

  @Test
  void transaction_canEqual_shouldReturnFalseForDifferentType() {
    Transaction tx = new Transaction();
    assertFalse(tx.canEqual("NotATransaction"));
  }

  @Test
  void transaction_toString_containsAllFields() {
    Instant now = Instant.now();
    Transaction tx = Transaction.builder()
      .id("1").accountId("2").accountNumber("3")
      .currency("USD").type("CREDIT")
      .amount(BigDecimal.ONE).timestamp(now)
      .status("OK").reason("Done")
      .build();

    String result = tx.toString();
    assertTrue(result.contains("Transaction"));
    assertTrue(result.contains("id=1"));
    assertTrue(result.contains("accountId=2"));
    assertTrue(result.contains("accountNumber=3"));
    assertTrue(result.contains("USD"));
  }

  @Test
  void transaction_defaultConstructor_shouldCreateEmptyObject() {
    Transaction transaction = new Transaction();

    assertAll(
      () -> assertNotNull(transaction),
      () -> assertNull(transaction.getId()),
      () -> assertNull(transaction.getAccountId()),
      () -> assertNull(transaction.getAccountNumber()),
      () -> assertNull(transaction.getCurrency()),
      () -> assertNull(transaction.getAmount()),
      () -> assertNull(transaction.getStatus()),
      () -> assertNull(transaction.getReason())
    );
  }
}
