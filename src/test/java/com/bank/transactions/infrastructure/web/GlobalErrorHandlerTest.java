package com.bank.transactions.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bank.transactions.domain.exception.BusinessException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

class GlobalErrorHandlerTest {

  private GlobalErrorHandler errorHandler = new GlobalErrorHandler();

  @Test
  void handleBusinessException() {
    BusinessException exception = new BusinessException("Test error");

    StepVerifier.create(errorHandler.handleBusinessException(exception))
      .expectNextMatches(response -> {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Test error", response.getBody().get("error"));
        return true;
      })
      .verifyComplete();
  }

  @Test
  void handleGenericException() {
    RuntimeException exception = new RuntimeException("Generic error");

    StepVerifier.create(errorHandler.handleGenericException(exception))
      .expectNextMatches(response -> {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("internal_server_error", response.getBody().get("error"));
        return true;
      })
      .verifyComplete();
  }

  @Test
  void handleBusinessException_responseStructure() {
    BusinessException exception = new BusinessException("account_not_found");

    StepVerifier.create(errorHandler.handleBusinessException(exception))
      .expectNextMatches(response -> {
        Map<String, Object> body = response.getBody();
        return response.getStatusCode() == HttpStatus.BAD_REQUEST &&
          body != null &&
          body.containsKey("error") &&
          "account_not_found".equals(body.get("error"));
      })
      .verifyComplete();
  }
}