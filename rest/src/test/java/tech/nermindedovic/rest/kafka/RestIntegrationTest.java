package tech.nermindedovic.rest.kafka;




import kafka.server.KafkaServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;
import tech.nermindedovic.rest.Topics;
import tech.nermindedovic.rest.api.RestAPI;
import tech.nermindedovic.rest.api.TransactionSearchService;
import tech.nermindedovic.rest.api.WebClientConfig;
import tech.nermindedovic.rest.kafka.balance.BalanceProducer;
import tech.nermindedovic.rest.kafka.balance.BalanceTestConfig;
import tech.nermindedovic.rest.kafka.transfer.MockSerdeConfig;
import tech.nermindedovic.rest.kafka.transfer.TransferFundsProducer;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {RestAPI.class, WebClientConfig.class, TransactionSearchService.class,  BalanceProducer.class, TransferFundsProducer.class, KafkaProperties.class},
properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {Topics.BALANCE_OUTBOUND, Topics.BALANCE_OUTBOUND, Topics.TRANSFER_OUTBOUND}, controlledShutdown = true)
@Import({MockSerdeConfig.class, BalanceTestConfig.class})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestIntegrationTest {

    @Autowired
    RestAPI restAPI;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @AfterAll
    void destroy() {
        embeddedKafkaBroker.getKafkaServers().forEach(KafkaServer::shutdown);
        embeddedKafkaBroker.getKafkaServers().forEach(KafkaServer::awaitShutdown);
    }




    @Test
    void sendTransferMessage() throws ExecutionException, InterruptedException {
        long creditorAN = 23424, creditorRN = 222;
        long debtorAN = 5345234, debtorRN = 111;
        BigDecimal amount = new BigDecimal("100.00");
        LocalDate date = LocalDate.now();
        String memo = "test memo";
        TransferMessage transferMessage = TransferMessage.builder()
                .messageId(0)
                .creditor(new Creditor(creditorAN, creditorRN))
                .debtor(new Debtor(debtorAN, debtorRN))
                .date(date)
                .amount(amount)
                .memo(memo)
                .build();


        assertThat(restAPI.fundsTransferRequest(transferMessage)).contains("has been sent successfully");
    }


    @Test
    void givenVALIDBalanceMessage_willSendAndReturnResponseFromTransformer()  {
        BalanceMessage balanceMessage = new BalanceMessage(1111,2222,"", false);
        BalanceMessage returned = restAPI.getBalanceUpdate(balanceMessage);
        balanceMessage.setBalance("10.00");
        assertThat(returned).isEqualTo(balanceMessage);

    }


}
