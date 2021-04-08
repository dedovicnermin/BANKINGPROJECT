package tech.nermindedovic.rest.kafka.transfer;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(topics = TransferErrorConsumerTest.TOPIC, ports = {9092})
@DirtiesContext
class TransferErrorConsumerTest {

    public static final String TOPIC = "funds.transfer.error";



    private Producer<String, String> producer;


    @SpyBean
    TransferErrorConsumer transferErrorConsumer;

    @BeforeEach
    void setTemplate() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        producer = new DefaultKafkaProducerFactory<>(properties, new StringSerializer(), new StringSerializer()).createProducer();

    }

    @AfterEach
    void shutdown() {
        producer.close();
    }




    @Test
    void listen_success() throws InterruptedException {
        producer.send(new ProducerRecord<>(TOPIC, "Error: could not validate both users"));
        producer.flush();
        verify(transferErrorConsumer, timeout(5000).times(1)).listen("Error: could not validate both users");

    }
}