package tech.nermindedovic.rest.kafka;





import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;
import tech.nermindedovic.rest.Topics;
import tech.nermindedovic.rest.api.RestAPI;
import tech.nermindedovic.rest.api.elastic.ElasticService;
import tech.nermindedovic.rest.api.WebClientConfig;
import tech.nermindedovic.rest.kafka.balance.BalanceProducer;
import tech.nermindedovic.rest.kafka.balance.BalanceTestConfig;
import tech.nermindedovic.rest.kafka.transfer.MockSerdeConfig;
import tech.nermindedovic.rest.kafka.transfer.TransferErrorConsumer;
import tech.nermindedovic.rest.kafka.transfer.TransferFundsProducer;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest(
        classes = {RestAPI.class, WebClientConfig.class, ElasticService.class,  BalanceProducer.class, TransferFundsProducer.class, KafkaProperties.class, TransferErrorConsumer.class}
)
@EmbeddedKafka(partitions = 1, topics = {Topics.BALANCE_OUTBOUND, Topics.BALANCE_INBOUND, Topics.TRANSFER_OUTBOUND, Topics.TRANSFER_ERROR}, controlledShutdown = true)
@Import({MockSerdeConfig.class, BalanceTestConfig.class})
@ActiveProfiles("integration")
@DirtiesContext
@EnableKafka
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class RestIntegrationTest {



    @Autowired
    RestAPI restAPI;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;





    // FOR ERRORS TEST
    BlockingQueue<ConsumerRecord<String, TransferMessage>> consumed;
    KafkaMessageListenerContainer<String, TransferMessage> listenerContainer;



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



    // @EnableKafka : using this annotation bc I chose not to include RestApplication.class and hence does not use @SpringBootApplication to
    //                automatically configure kafkaListenerEndpointRegistry


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Test
    @Timeout(15)
    void whenTransferErrorArrives_errorConsumerWillConsume() throws InterruptedException {
        setUpErrorConsumption();

        ConcurrentMessageListenerContainer<?, ?> container = (ConcurrentMessageListenerContainer<?, ?>) kafkaListenerEndpointRegistry.getListenerContainer("dlqListener");
        container.stop();

        @SuppressWarnings("unchecked")
        AcknowledgingConsumerAwareMessageListener<String, String> messageListener = (AcknowledgingConsumerAwareMessageListener<String, String>) container.getContainerProperties().getMessageListener();
        CountDownLatch latch = new CountDownLatch(1);
        container.getContainerProperties().setMessageListener((AcknowledgingConsumerAwareMessageListener<String, String>) (consumerRecord, acknowledgment, consumer) -> {
            messageListener.onMessage(consumerRecord, acknowledgment, consumer);
            latch.countDown();
        });
        container.start();

        Producer<String, String> producer = configureProducer();
        producer.send(new ProducerRecord<>(Topics.TRANSFER_ERROR, "This is an error sent from either persistence/transformer/router when it has been unable to process the request sent"));
        producer.flush();

        Assertions.assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        producer.close();
        listenerContainer.stop();
    }



    private Producer<String, String> configureProducer() {
        Map<String, Object> producerConfig = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        return new DefaultKafkaProducerFactory<>(producerConfig, new StringSerializer(), new StringSerializer()).createProducer();
    }


    private void setUpErrorConsumption() {
        Map<String,Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("test-errorConsumer", "true", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, TransferMessage> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new JsonDeserializer<>(TransferMessage.class));
        ContainerProperties containerProperties = new ContainerProperties(Topics.TRANSFER_OUTBOUND);
        listenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        consumed = new LinkedBlockingQueue<>();

        listenerContainer.setupMessageListener((MessageListener<String, TransferMessage>) consumed::add);
        listenerContainer.start();
        ContainerTestUtils.waitForAssignment(listenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
    }


















}
