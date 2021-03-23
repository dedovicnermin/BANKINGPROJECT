package tech.nermindedovic.persistence.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.repository.AccountRepository;



import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountRepoTest {


    @Autowired
    private AccountRepository repository;

    @Test
    void givenAccount_whenSave_thenGetOk() {
        Account account = new Account(1L,1L, "Bob", 200.00);
        repository.save(account);

        Account account1 = repository.findById(1L).get();
        assertEquals("Bob", account1.getUserName());
    }


    @Test
    void givenNonExisting_whenRetrieving_emptyOptional_test() {

        Optional<Account> optionalAccount = repository.findById(1999L);
        assertFalse(optionalAccount.isPresent());
    }

}
