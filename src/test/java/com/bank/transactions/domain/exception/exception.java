package com.bank.transactions.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BusinessExceptionTest {

  @Test
  void businessException_withMessage() {
    BusinessException exception = new BusinessException("Test error");
    assertEquals("Test error", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void businessException_withMessageAndCause() {
    RuntimeException cause = new RuntimeException("Root cause");
    BusinessException exception = new BusinessException("Test error", cause);

    assertEquals("Test error", exception.getMessage());
    assertEquals(cause, exception.getCause());
    assertEquals("Root cause", exception.getCause().getMessage());
  }
}