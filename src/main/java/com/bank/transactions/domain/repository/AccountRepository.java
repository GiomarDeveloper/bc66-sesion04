package com.bank.transactions.domain.repository;

import com.bank.transactions.domain.model.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para gestionar las operaciones CRUD de {@link Account}.
 */
public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

  /**
     * Busca una cuenta por su número.
     *
     * @param number número de cuenta
     * @return {@link Mono} con la cuenta encontrada, o vacío si no existe
   */
  Mono<Account> findByNumber(String number);
}
