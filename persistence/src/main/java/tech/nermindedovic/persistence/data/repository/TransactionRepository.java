package tech.nermindedovic.persistence.data.repository;


import org.springframework.data.repository.CrudRepository;
import tech.nermindedovic.persistence.data.entity.Transaction;

import java.util.Optional;


public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(Long transactionId);


}
