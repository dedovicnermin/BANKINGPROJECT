package tech.nermindedovic.persistence.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tech.nermindedovic.persistence.data.entity.Account;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {




}
