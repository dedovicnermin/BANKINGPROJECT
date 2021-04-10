package tech.nermindedovic.persistence.data;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.repository.AccountRepository;


import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext
class AccountRepoTest {


    @Autowired
    private AccountRepository repository;




    @Test
    void givenAccount_whenSave_thenGetOk() {
        Account account = new Account(1L,1L, "Bob", new BigDecimal("200.00"));
        repository.save(account);
        assertEquals(1,repository.count());
        Account account1 = repository.findById(1L).get();
        assertEquals("Bob", account1.getUserName());
        assertEquals(1L, account1.getRoutingNumber());


    }



    @Test
    void givenNonExisting_whenRetrieving_emptyOptional_test() {
        Optional<Account> optionalAccount = repository.findById(1999L);
        assertFalse(optionalAccount.isPresent());
    }

}
