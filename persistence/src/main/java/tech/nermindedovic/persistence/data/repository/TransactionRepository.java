package tech.nermindedovic.persistence.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import tech.nermindedovic.persistence.data.entity.Transaction;



public interface TransactionRepository extends CrudRepository<Transaction, Long> {



}
