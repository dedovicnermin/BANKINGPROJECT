package tech.nermindedovic.persistence.data;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
class AccountRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository repository;


    @Test
    void givenAccount_whenSave_thenGetOk() {
        Account account = new Account(1L,1L, "Bob", new BigDecimal("200.00"));
        entityManager.persist(account);
        Account actual = repository.findById(1L).get();
        assertEquals(account.getUserName(), actual.getUserName());
        assertEquals(account.getRoutingNumber(), actual.getRoutingNumber());
        assertEquals(account.getAccountBalance(), actual.getAccountBalance());
    }

    @Test
    void givenNonExisting_whenRetrieving_emptyOptional_test() {
        Optional<Account> optionalAccount = repository.findById(1999L);
        assertFalse(optionalAccount.isPresent());
    }




}
