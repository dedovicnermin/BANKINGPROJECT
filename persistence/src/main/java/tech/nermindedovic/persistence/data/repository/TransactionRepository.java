package tech.nermindedovic.persistence.data.repository;

import org.springframework.data.repository.CrudRepository;
import tech.nermindedovic.persistence.data.entity.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {



}
