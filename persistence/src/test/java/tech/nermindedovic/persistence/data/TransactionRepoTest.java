package tech.nermindedovic.persistence.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionRepoTest {

    @Autowired
    private TransactionRepository transactionRepository;




    @Test
    void givenTransaction_whenSave_thenOK() {
        Transaction transaction = new Transaction();
        transaction.setCreditorAccountNumber(12);
        transaction.setDebtorAccountNumber(1);
        transaction.setAmount(new BigDecimal(100));
        transaction.setDate(new Date());
        transaction.setMemo("Memo");

        transactionRepository.save(transaction);

        assertThat(transactionRepository.findAll()).hasSize(1);
        assertThat(transactionRepository.findAll().iterator().next().getTransactionId()).isEqualTo(transaction.getTransactionId());


    }

    @Test
    void test_allConstructorArgs_success() {
        Transaction transaction = new Transaction(1,1,1,new BigDecimal(10),new Date(), "memo");

        Assertions.assertAll(() -> {
            assertThat(transaction).isNotNull();
            assertThat(transaction.getCreditorAccountNumber()).isNotNull();
            assertThat(transaction.getDebtorAccountNumber()).isNotNull();
            assertThat(transaction.getMemo()).isNotEmpty();
            assertThat(transaction.getAmount()).isNotNull();
            assertThat(transaction.getDate()).isNotNull();
        });
    }

    @Test
    void test_TransactionWillAutoGenerateId() {
        Transaction transaction = new Transaction();
        assertThat(transaction.getTransactionId()).isNotNull();
    }

    @Test
    void givenNonExistingTransaction_returnsEmptyOptional() {
        assertThat(transactionRepository.findById(1l).isPresent()).isEqualTo(false);
    }







}
