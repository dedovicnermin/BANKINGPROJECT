package tech.nermindedovic.rest.kafka.transfer;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.rest.business.domain.Creditor;
import tech.nermindedovic.rest.business.domain.Debtor;
import tech.nermindedovic.rest.business.domain.TransferMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = {TransferFundsProducer.class, TransferMessageConfiguration.class}, properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = "funds.transformer.request")
@DirtiesContext
class TransferFundsProducerTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;


    @Autowired
    KafkaTemplate<String, TransferMessage> template;

    @Autowired
    TransferFundsProducer transferFundsProducer;


    @Test
    void test_TMproducer_sendTransferMessage_success() throws ExecutionException, InterruptedException {
        TransferMessage transferMessage = TransferMessage.builder()
                .message_id(UUID.randomUUID().getLeastSignificantBits())
                .date(LocalDate.now())
                .memo("Generic memo")
                .amount(BigDecimal.TEN)
                .debtor(new Debtor(1111,2222))
                .creditor(new Creditor(33333,44444))
                .build();


        assertThat(transferFundsProducer.sendTransferMessage(transferMessage)).contains("has been sent successfully");
    }



}