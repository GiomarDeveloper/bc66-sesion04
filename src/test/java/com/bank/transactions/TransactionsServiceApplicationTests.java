package com.bank.transactions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TransactionsServiceApplicationTests {

  @Test
  void contextLoads() {
    TransactionsServiceApplication.main(new String[] {});
    assertTrue(true); // Si llega aquí, el contexto arrancó
  }
}
