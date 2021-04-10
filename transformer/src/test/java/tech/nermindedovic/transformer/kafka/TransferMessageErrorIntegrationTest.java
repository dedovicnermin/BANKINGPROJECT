package tech.nermindedovic.transformer.kafka;



import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {TransferMessageErrorIntegrationTest.INBOUND_TOPIC, TransferMessageErrorIntegrationTest.OUTBOUND_TOPIC})
@ExtendWith({SpringExtension.class})
@DirtiesContext
class TransferMessageErrorIntegrationTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    public static final String INBOUND_TOPIC = TransformerTopicNames.INBOUND_REST_TRANSFER;
    public static final String OUTBOUND_TOPIC = TransformerTopicNames.OUTBOUND_TRANSFER_ERRORS;



    BlockingQueue<ConsumerRecord<String, String>> records;
    KafkaMessageListenerContainer<String,String> container;

    Producer<String, String> badProducer;

    @BeforeEach
    void setup() {
        Map<String, Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "true", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(OUTBOUND_TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String,String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerConfigs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        badProducer = new DefaultKafkaProducerFactory<>(producerConfigs, new StringSerializer(), new StringSerializer()).createProducer();
    }

    @AfterEach
    void shutdown() {
        container.stop();
        badProducer.close();
    }



    @Test
    void listenerContainerCantDeserialize_errorHandlerSendsToErrorTopic_test() throws InterruptedException {

        badProducer.send(new ProducerRecord<>(INBOUND_TOPIC, "This is NOT a transfer message. This should not make it past one line of .listen code and container will delegate to error message handler"));
        badProducer.flush();


        ConsumerRecord<String, String> record = records.poll(100, TimeUnit.MILLISECONDS);
        assertThat(record).isNotNull();
        assertThat(record.value()).contains("Error deserializing key/value for partition funds.");


    }
}
