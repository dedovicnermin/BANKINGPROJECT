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
        assertThat(transactionRepository.findById(1L)).isEmpty();
    }

    @Test
    void confirmLocalDate_outputsAsExpected() {
        LocalDate localDate = LocalDate.now();
        int month = localDate.getMonthValue();
        int year = localDate.getYear();
        int day = localDate.getDayOfMonth();

        Transaction transaction = new Transaction(123L, 11L, 22L, BigDecimal.TEN, localDate, "memo");
        assertThat(transaction.getDate()).hasToString(year + "-" + (month < 10 ? "0"+month : month) + "-" + (day < 10 ? "0" + day : day));
    }

    @Test
    void confirmBuilderWorksAsIntended() {
        Transaction transaction = Transaction.builder()
                .transactionId(1L)
                .creditorAccountNumber(100L)
                .debtorAccountNumber(101L)
                .amount(BigDecimal.ONE)
                .date(LocalDate.now())
                .memo("memo")
                .build();

        assertThat(transaction.toString()).hasToString(new Transaction(1L, 100L, 101L, BigDecimal.ONE, LocalDate.now(), "memo").toString());
    }










}
