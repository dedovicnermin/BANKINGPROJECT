package tech.nermindedovic.persistence.data;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;


import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;



@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionRepoTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    EntityManager entityManager;


    @Test
    void givenTransaction_whenSave_thenOK() {
        Transaction transaction = new Transaction();
        transaction.setCreditorAccountNumber(12);
        transaction.setDebtorAccountNumber(1);
        transaction.setAmount(new BigDecimal(100));
        transaction.setDate(LocalDate.now());
        transaction.setMemo("Memo");

        transactionRepository.save(transaction);

        assertThat(transactionRepository.findAll()).hasSize(1);
        assertThat(transactionRepository.findAll().iterator().next().getTransactionId()).isEqualTo(transaction.getTransactionId());
    }


    @Test
    void givenNonExistingTransaction_returnsEmptyOptional() {
        assertThat(transactionRepository.findById(1L).isPresent()).isFalse();
    }










}
