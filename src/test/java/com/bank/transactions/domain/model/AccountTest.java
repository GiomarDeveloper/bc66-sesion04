package com.bank.transactions.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AccountTest {

  @Test
  void account_settersAndGetters() {
    Account account = new Account();
    account.setId("acc-123");
    account.setNumber("001-0001");
    account.setBalance(new BigDecimal("1000.50"));
    account.setCurrency("USD");

    assertEquals("acc-123", account.getId());
    assertEquals("001-0001", account.getNumber());
    assertEquals(new BigDecimal("1000.50"), account.getBalance());
    assertEquals("USD", account.getCurrency());
  }

  @Test
  void account_builder() {
    Account account = Account.builder()
      .id("acc-456")
      .number("001-0002")
      .balance(new BigDecimal("500.75"))
      .currency("EUR")
      .build();

    assertAll(
      () -> assertEquals("acc-456", account.getId()),
      () -> assertEquals("001-0002", account.getNumber()),
      () -> assertEquals(new BigDecimal("500.75"), account.getBalance()),
      () -> assertEquals("EUR", account.getCurrency())
    );
  }

  @Test
  void account_equalsAndHashCode() {
    Account a1 = Account.builder()
      .id("id1").number("001").balance(new BigDecimal("10")).currency("USD").build();
    Account a2 = Account.builder()
      .id("id1").number("001").balance(new BigDecimal("10")).currency("USD").build();

    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
  }

  @Test
  void account_toString_containsClassName() {
    Account account = new Account();
    account.setNumber("001");
    assertTrue(account.toString().contains("Account"));
  }

  @Test
  void account_defaultConstructor_notNull() {
    Account account = new Account();
    assertNotNull(account);
  }

  @Test
  void account_equalsWithDifferentObjectOrNull() {
    Account account = Account.builder()
      .id("id1")
      .number("001")
      .balance(new BigDecimal("10"))
      .currency("USD")
      .build();

    assertNotEquals(null, account);
    assertNotEquals(new Object(), account);
  }
}
