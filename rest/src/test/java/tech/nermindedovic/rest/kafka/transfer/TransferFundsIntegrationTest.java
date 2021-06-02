package tech.nermindedovic.rest.kafka.transfer;



import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import tech.nermindedovic.library.pojos.TransferMessage;
import tech.nermindedovic.rest.Topics;
import tech.nermindedovic.rest.api.RestAPI;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {TransferFundsIntegrationTest.ERROR_TOPIC, Topics.BALANCE_OUTBOUND})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferFundsIntegrationTest {

    public static final String ERROR_TOPIC = "funds.transfer.error";


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired RestAPI restAPI;
    @SpyBean
    TransferErrorConsumer transferErrorConsumer;

    BlockingQueue<ConsumerRecord<String, TransferMessage>> consumed;
    KafkaMessageListenerContainer<String, TransferMessage> listenerContainer;

    @BeforeAll
    void setup() {
        System.setProperty("spring.kafka.bootstrap-servers", embeddedKafkaBroker.getBrokersAsString());
        setup_receivingEnd();
    }

    @AfterAll
    void destroyBroker()  {
        shutdown();
        embeddedKafkaBroker.destroy();
        kafkaListenerEndpointRegistry.destroy();
    }



    /**
     * testing error consumer will listen to messages on inbound
     * overrides errorMessage listener functionality to use countDown latch
     */


    @Test
    @Timeout(15)
    void whenTransferErrorArrives_errorConsumerWillConsume() throws InterruptedException {
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
        producer.send(new ProducerRecord<>(ERROR_TOPIC, "This is an error sent from either persistence/transformer/router when it has been unable to process the request sent"));
        producer.flush();

        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        producer.close();

    }






    void setup_receivingEnd() {
        Map<String,Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("test", "true", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, TransferMessage> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new JsonDeserializer<>(TransferMessage.class));
        ContainerProperties containerProperties = new ContainerProperties(Topics.TRANSFER_OUTBOUND);
        listenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        consumed = new LinkedBlockingQueue<>();

        listenerContainer.setupMessageListener((MessageListener<String, TransferMessage>) consumed::add);
        listenerContainer.start();
        ContainerTestUtils.waitForAssignment(listenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
    }


    void shutdown() {
        listenerContainer.stop();
    }


    private Producer<String, String> configureProducer() {
        Map<String, Object> producerConfig = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        return new DefaultKafkaProducerFactory<>(producerConfig, new StringSerializer(), new StringSerializer()).createProducer();
    }






}
