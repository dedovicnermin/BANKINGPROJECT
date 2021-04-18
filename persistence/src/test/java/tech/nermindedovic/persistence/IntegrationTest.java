package tech.nermindedovic.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import tech.nermindedovic.persistence.business.doman.Creditor;
import tech.nermindedovic.persistence.business.doman.Debtor;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.business.service.ConsumerService;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;


import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Slf4j
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class IntegrationTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ConsumerService consumerService;



    @Test
    void test_onProcessTransferRequest_shouldNotThrow_and_accountsUpdated_and_transactionPersisted() throws JsonProcessingException {
        accountRepository.save(new Account(1,123,"BOB", new BigDecimal("1000")));
        accountRepository.save(new Account(2, 456, "GREG", new BigDecimal("600")));


        Creditor creditor = new Creditor(1, 123);
        Debtor debtor = new Debtor(2, 456);
        TransferMessage transferMessage = new TransferMessage(1, creditor, debtor, LocalDate.now(),new BigDecimal("25.00"),  "MEMO");
        String xml = mapper.writeValueAsString(transferMessage);
        TransferMessage formattedTM = mapper.readValue(xml, TransferMessage.class);


        consumerService.handleFundsTransferRequest(xml);

        Transaction transaction = transactionRepository.findAll().iterator().next();

        Assertions.assertAll(() -> {
            Assertions.assertDoesNotThrow((ThrowingSupplier<RuntimeException>) RuntimeException::new);
            Assertions.assertEquals(accountRepository.findById(1L).get().getAccountBalance(), new BigDecimal("1025.00"));
            Assertions.assertEquals(new BigDecimal("575.00"), accountRepository.findById(2L).get().getAccountBalance());
            assertThat(transaction.getCreditorAccountNumber()).isEqualTo(creditor.getAccountNumber());
            assertThat(transaction.getDebtorAccountNumber()).isEqualTo(debtor.getAccountNumber());
            assertThat(transaction.getMemo()).isEqualTo(formattedTM.getMemo());
            assertThat(transaction.getAmount()).isEqualTo(new BigDecimal(String.valueOf(formattedTM.getAmount())));

        });

    }

    @Test
    void test_onProcessTransferRequest_withInvalidData_shouldThrow_RuntimeException() {
        String xml = "<ERROR></ERROR>";
        Assertions.assertThrows(RuntimeException.class,() -> consumerService.handleFundsTransferRequest(xml));
    }



    public XmlMapper mapper = new XmlMapper();


}
