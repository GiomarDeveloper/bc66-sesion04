package com.bank.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del microservicio de transacciones bancarias.
 * Inicia la aplicación Spring Boot.
 */
@SpringBootApplication(scanBasePackages = "com.bank.transactions")
public class TransactionsServiceApplication {

  /**
   * Método principal que inicia el servicio de transacciones.
   *
   * @param args argumentos de línea de comando
   */
  public static void main(String[] args) {
    SpringApplication.run(TransactionsServiceApplication.class, args);
  }
}