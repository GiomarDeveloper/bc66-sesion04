package com.bank.transactions.application.dto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateTxRequestTest {

  private static Validator validator;

  @BeforeAll
  static void setupValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void createTxRequest_settersAndGetters() {
    CreateTxRequest request = new CreateTxRequest();
    request.setAccountNumber("001-0001");
    request.setType("DEBIT");
    request.setAmount(new BigDecimal("150.75"));
    request.setCurrency("USD");

    assertEquals("001-0001", request.getAccountNumber());
    assertEquals("DEBIT", request.getType());
    assertEquals(new BigDecimal("150.75"), request.getAmount());
    assertEquals("USD", request.getCurrency());
  }

  @Test
  void createTxRequest_allFields() {
    CreateTxRequest request = new CreateTxRequest();
    request.setAccountNumber("001-0002");
    request.setType("CREDIT");
    request.setAmount(new BigDecimal("300.25"));
    request.setCurrency("EUR");

    assertAll(
      () -> assertEquals("001-0002", request.getAccountNumber()),
      () -> assertEquals("CREDIT", request.getType()),
      () -> assertEquals(new BigDecimal("300.25"), request.getAmount()),
      () -> assertEquals("EUR", request.getCurrency())
    );
  }

  @Test
  void validate_missingFields_shouldReturnViolations() {
    CreateTxRequest invalidRequest = new CreateTxRequest();
    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(invalidRequest);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("required")));
  }

  @Test
  void validate_amountTooLow_shouldFailDecimalMin() {
    CreateTxRequest request = new CreateTxRequest();
    request.setAccountNumber("001-0003");
    request.setType("DEBIT");
    request.setAmount(new BigDecimal("0.00"));
    request.setCurrency("USD");

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("at least 0.01")));
  }

  @Test
  void equalsAndHashCode_shouldWorkAsExpected() {
    CreateTxRequest req1 = new CreateTxRequest();
    req1.setAccountNumber("001");
    req1.setType("DEBIT");
    req1.setAmount(new BigDecimal("10.00"));
    req1.setCurrency("USD");

    CreateTxRequest req2 = new CreateTxRequest();
    req2.setAccountNumber("001");
    req2.setType("DEBIT");
    req2.setAmount(new BigDecimal("10.00"));
    req2.setCurrency("USD");

    assertEquals(req1, req2);
    assertEquals(req1.hashCode(), req2.hashCode());
    assertTrue(req1.toString().contains("accountNumber"));
  }
}
