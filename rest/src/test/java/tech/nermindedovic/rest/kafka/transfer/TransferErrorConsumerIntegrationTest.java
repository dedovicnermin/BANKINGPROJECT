package tech.nermindedovic.rest.kafka.transfer;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = TransferErrorConsumerIntegrationTest.ERROR_TOPIC)
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class TransferErrorConsumerIntegrationTest {
    public static final String ERROR_TOPIC = "funds.transfer.error";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    BlockingQueue<ConsumerRecord<String, String>> consumed;
    KafkaMessageListenerContainer<String, String> listenerContainer;

    Producer<String, String> errorProducer;

    @SpyBean
    TransferErrorConsumer transferErrorConsumer;

    @BeforeEach
    void setup() {
        Map<String, Object> consumerConfigs = new HashMap<>(KafkaTestUtils.consumerProps("rest-error-consumer-test", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfigs, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(ERROR_TOPIC);
        listenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        consumed = new LinkedBlockingQueue<>();
        listenerContainer.setupMessageListener((MessageListener<String, String>) consumed::add);
        listenerContainer.start();
        ContainerTestUtils.waitForAssignment(listenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerConfigs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        errorProducer = new DefaultKafkaProducerFactory<>(producerConfigs, new StringSerializer(), new StringSerializer()).createProducer();
    }

    @AfterEach
    void shutdown() {
        listenerContainer.stop();
        errorProducer.close();
    }


    @Test
    void transferMessageErrorConsumer_listensForErrorRecords_sendByOutsideApp() throws InterruptedException {
        String errorMessage = "Persistence/Transformer: error processing TransferMessage sent from REST";
        errorProducer.send(new ProducerRecord<>(ERROR_TOPIC, errorMessage));
        errorProducer.flush();

        ConsumerRecord<String,String> consumedRecord = consumed.poll(1000, TimeUnit.MILLISECONDS);
        assertThat(consumedRecord).isNotNull();
        assertThat(consumedRecord.value()).isEqualTo(errorMessage);

        Mockito.verify(transferErrorConsumer, Mockito.times(1)).listen(errorMessage);
    }

}
