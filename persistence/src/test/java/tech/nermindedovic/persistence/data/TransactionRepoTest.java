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
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

//@SpringBootTest
@DataJpaTest
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
        Transaction transaction = new Transaction(1L,1,1,new BigDecimal(10),new Date(), "memo");

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

    @Test
    void givenDuplicateTransferMessage_throwsInvalidTransferMessage() {
        Creditor creditor = new Creditor(1111, 1000000001);
        Debtor debtor = new Debtor(222222, 1000002222);
        TransferMessage transferMessage = new TransferMessage(8080, creditor, debtor, new Date(), new BigDecimal("20.50"), "lunch time special");
        Transaction transaction = Transaction.builder()
                .transactionId(transferMessage.getMessage_id())
                .debtorAccountNumber(debtor.getAccountNumber())
                .creditorAccountNumber(creditor.getAccountNumber())
                .amount(transferMessage.getAmount())
                .date(transferMessage.getDate())
                .memo(transferMessage.getMemo())
                .build();
        Transaction transactionDuplicate = new Transaction(transaction.getTransactionId(), transaction.getCreditorAccountNumber(), transaction.getDebtorAccountNumber(), transaction.getAmount(), transaction.getDate(), transaction.getMemo());

    }







}
