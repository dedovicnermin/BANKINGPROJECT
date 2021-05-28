package tech.nermindedovic.rest.avro;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import tech.nermindedovic.Creditor;
import tech.nermindedovic.Debtor;
import tech.nermindedovic.TransferMessage;
import tech.nermindedovic.rest.Topics;
import tech.nermindedovic.rest.api.RestAPI;
import tech.nermindedovic.rest.api.RestAvroAPI;
import tech.nermindedovic.rest.api.TransactionSearchService;
import tech.nermindedovic.rest.avro.configuration.CustomAvroDeserializer;
import tech.nermindedovic.rest.avro.configuration.TransferMessageAvroConfig;
import tech.nermindedovic.rest.kafka.balance.avro.BalanceProducer;
import tech.nermindedovic.rest.kafka.transfer.avro.TransferFundsProducer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

//
//@EmbeddedKafka(partitions = 1, topics = {Topics.TRANSFER_OUTBOUND})
//@ExtendWith({MockitoExtension.class, SpringExtension.class})
//@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers"}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DirtiesContext
//@Import(TransferMessageAvroConfig.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@Slf4j
public class TransferIntegrationTest {

//    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
//    @Autowired
//    private EmbeddedKafkaBroker embeddedKafkaBroker;
//
//    @MockBean
//    BalanceProducer balanceProducer;
//    @MockBean
//    WebClient webClient;
//    @MockBean
//    TransactionSearchService transactionSearchService;
//
//
//    KafkaTemplate<String, TransferMessage> kafkaTemplate = new TransferMessageAvroConfig().transferMessageKafkaTemplateTest();
//
//    TransferFundsProducer transferFundsProducer = new TransferFundsProducer(kafkaTemplate);
//
//
//
//    private final RestAvroAPI restAPI = new RestAvroAPI(balanceProducer, transferFundsProducer, webClient, transactionSearchService);
//
//    BlockingQueue<ConsumerRecord<String, TransferMessage>> consumed;
//    KafkaMessageListenerContainer<String, TransferMessage> listenerContainer;
//
//    @BeforeAll
//    void startup() {
//        System.setProperty("spring.kafka.bootstrap-servers", embeddedKafkaBroker.getBrokersAsString());
//        setup_receivingEnd();
//    }
//
//    @AfterAll
//    void destroy() {
//        listenerContainer.stop();
//        embeddedKafkaBroker.destroy();
//    }
//
//
//    void setup_receivingEnd() {
//        Map<String,Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("test-avro-consumer", "true", embeddedKafkaBroker));
//        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new CustomAvroDeserializer());
//        ContainerProperties containerProperties = new ContainerProperties(Topics.TRANSFER_OUTBOUND);
//        listenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
//        consumed = new LinkedBlockingQueue<>();
//        listenerContainer.setupMessageListener((MessageListener<String, TransferMessage>) consumed::add);
//        listenerContainer.start();
//        ContainerTestUtils.waitForAssignment(listenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
//    }
//
//    @Test
//    public void givenValidTransferMessage_willProduceToTransformerTopic_Avro() throws ExecutionException, InterruptedException {
//        long msgId = 1L;
//        long debtorAN = 123L, debtorRN = 111L;
//        long creditorAN = 345L, creditorRN = 222L;
//        String date = LocalDate.now().toString();
//        String amount = "10.00";
//        String memo = "my memo";
//
//        log.info(embeddedKafkaBroker.getBrokersAsString() + "\n\n\n");
//
//        TransferMessage transferMessage = TransferMessage.newBuilder()
//                .setMessageId(msgId)
//                .setDebtor(new Debtor(debtorAN, debtorRN))
//                .setCreditor(new Creditor(creditorAN, creditorRN))
//                .setAmount(amount)
//                .setDate(date)
//                .setMemo(memo)
//                .build();
//
//
//        restAPI.fundsTransferRequest(transferMessage);
//        ConsumerRecord<String, TransferMessage> consumerRecord = consumed.poll(200, TimeUnit.MILLISECONDS);
//        assertThat(consumerRecord).isNotNull();
//        assertThat(consumerRecord.value()).isEqualTo(transferMessage);
//    }


}
