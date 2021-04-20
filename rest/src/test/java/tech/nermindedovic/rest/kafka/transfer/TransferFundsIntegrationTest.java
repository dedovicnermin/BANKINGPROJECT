package tech.nermindedovic.rest.kafka.transfer;



import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.rest.api.RestAPI;
import tech.nermindedovic.rest.business.domain.Creditor;
import tech.nermindedovic.rest.business.domain.Debtor;
import tech.nermindedovic.rest.business.domain.TransferMessage;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {TransferFundsIntegrationTest.ERROR_TOPIC, TransferFundsIntegrationTest.OUTBOUND_TOPIC })
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferFundsIntegrationTest {

    public static final String OUTBOUND_TOPIC = "funds.transformer.request";
    public static final String ERROR_TOPIC = "funds.transfer.error";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired RestAPI restAPI;
    @SpyBean private TransferErrorConsumer transferErrorConsumer;

    BlockingQueue<ConsumerRecord<String, TransferMessage>> consumed;
    KafkaMessageListenerContainer<String, TransferMessage> listenerContainer;

    @BeforeAll
    void setup() {
        setup_receivingEnd();
    }

    @AfterAll
    void destroyBroker() {
        shutdown();
        embeddedKafkaBroker.destroy();
    }


    /**
     * testing if producer will producer to transfer topic
     */
    @Test
    void givenValidTransferMessage_willProduceToTransformerTopic() throws ExecutionException, InterruptedException {
        TransferMessage transferMessage = TransferMessage.builder()
                .message_id(0)
                .creditor(new Creditor(1111, 1111))
                .debtor(new Debtor(2222,2222))
                .amount(BigDecimal.TEN)
                .memo("Message sent from rest application")
                .date(LocalDate.now())
                .build();


        restAPI.fundsTransferRequest(transferMessage);
        ConsumerRecord<String, TransferMessage> consumerRecord = consumed.poll(100, TimeUnit.MILLISECONDS);
        assertThat(consumerRecord).isNotNull();
        assertThat(consumerRecord.value()).isEqualTo(transferMessage);

    }


    /**
     * testing error consumer will listen to messages on inbound
     */
    @Test
    void whenTransferErrorArrives_errorConsumerWillConsume()  {

        Producer<String, String> producer = configureProducer();
        producer.send(new ProducerRecord<>(ERROR_TOPIC, "This is an error sent from either persistence or transformer when it has been unable to process the request sent"));
        producer.flush();

        Mockito.verify(transferErrorConsumer, timeout(5000).times(1)).listen(anyString());

        producer.close();
    }






    void setup_receivingEnd() {
        Map<String,Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("test", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, TransferMessage> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new JsonDeserializer<>(TransferMessage.class));
        ContainerProperties containerProperties = new ContainerProperties(OUTBOUND_TOPIC);
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
