package tech.nermindedovic.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.nermindedovic.persistence.business.components.MsgProcessor;
import tech.nermindedovic.persistence.business.components.XMLProcessor;
import tech.nermindedovic.persistence.business.doman.Creditor;
import tech.nermindedovic.persistence.business.doman.Debtor;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.business.service.ConsumerService;
import tech.nermindedovic.persistence.business.service.PersistenceService;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;

import java.util.Date;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class IntegrationTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ConsumerService consumerService;

    @Autowired
    MsgProcessor msgProcessor;

    @Autowired
    XMLProcessor xmlProcessor;

    @Autowired
    PersistenceService persistenceService;




    @Test
    void test_onProcessTransferRequest_shouldNotThrow_and_accountsUpdated_and_transactionPersisted() throws JsonProcessingException {
        accountRepository.save(new Account(1,123,"BOB", 1000));
        accountRepository.save(new Account(2, 456, "GREG", 600));


        Creditor creditor = new Creditor(1, 123);
        Debtor debtor = new Debtor(2, 456);
        TransferMessage transferMessage = new TransferMessage(1, creditor, debtor,new Date(),25,  "MEMO");
        String xml = mapper.writeValueAsString(transferMessage);
        TransferMessage formattedTM = mapper.readValue(xml, TransferMessage.class);


        consumerService.handleFundsTransferRequest(xml);

        Transaction transaction = transactionRepository.findAll().iterator().next();

        Assertions.assertAll(() -> {
            Assertions.assertDoesNotThrow(() -> new RuntimeException());
            Assertions.assertTrue(accountRepository.findById(1l).get().getAccountBalance() == 1025);
            Assertions.assertTrue(accountRepository.findById(2l).get().getAccountBalance() == 575);
            assertThat(transaction.getCreditorAccountNumber()).isEqualTo(creditor.getAccountNumber());
            assertThat(transaction.getDebtorAccountNumber()).isEqualTo(debtor.getAccountNumber());
            assertThat(transaction.getMemo()).isEqualTo(formattedTM.getMemo());
            assertThat(transaction.getAmount()).isEqualTo(formattedTM.getAmount());

        });

    }

    @Test
    void test_onProcessTransferRequest_withInvalidData_shouldThrow_RuntimeException() {
        String xml = "<ERROR></ERROR>";
        Assertions.assertThrows(RuntimeException.class,() -> consumerService.handleFundsTransferRequest(xml));
    }

    @Test
    void test_onProcessTransferRequest_withInvalidUsers_shouldThrow_RunTimeException() throws JsonProcessingException {
        Creditor creditor = new Creditor(1, 123);
        Debtor debtor = new Debtor(2, 456);
        TransferMessage transferMessage = new TransferMessage(1, creditor, debtor,new Date(),-25,  "MEMO");
        String xml = mapper.writeValueAsString(transferMessage);
        TransferMessage formattedTM = mapper.readValue(xml, TransferMessage.class);



        Assertions.assertThrows(RuntimeException.class, () -> consumerService.handleFundsTransferRequest(xml));
    }

    public XmlMapper mapper = new XmlMapper();


}
