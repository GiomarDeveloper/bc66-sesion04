package com.bank.transactions.domain.repository;

import com.bank.transactions.domain.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Repositorio reactivo para las operaciones con {@link Transaction}.
 */
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

  /**
   * Obtiene las transacciones de una cuenta ordenadas por fecha descendente.
   *
   * @param accountId identificador de la cuenta
   * @return {@link Flux} con las transacciones de la cuenta
   */
  Flux<Transaction> findByAccountIdOrderByTimestampDesc(String accountId);
}
