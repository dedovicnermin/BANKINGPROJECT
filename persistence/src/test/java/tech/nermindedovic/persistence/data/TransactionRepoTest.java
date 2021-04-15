package tech.nermindedovic.persistence.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.persistence.business.doman.Creditor;
import tech.nermindedovic.persistence.business.doman.Debtor;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

//@SpringBootTest
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionRepoTest {

    @Autowired
    private TransactionRepository transactionRepository;




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
        assertThat(transactionRepository.findById(1l).isPresent()).isEqualTo(false);
    }








}
