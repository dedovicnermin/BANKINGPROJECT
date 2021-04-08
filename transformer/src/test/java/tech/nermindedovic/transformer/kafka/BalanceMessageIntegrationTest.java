package tech.nermindedovic.transformer.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.nermindedovic.transformer.pojos.BalanceMessage;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;




@SpringBootTest
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@EmbeddedKafka(topics = {"balance.transformer.request", "balance.transformer.response", "balance.update.request"}, partitions = 1, ports = 9092)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BalanceMessageIntegrationTest {

    @SpyBean
    TransformerProducer transformerProducer;

    private Producer<String, BalanceMessage> producer;
    private Consumer<String, BalanceMessage> consumer;


    @BeforeEach
    void setup() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BalanceMessage.class);
        consumerProps.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, true);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.TYPE_MAPPINGS, BalanceMessage.class);

        consumer = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new JsonDeserializer<BalanceMessage>()).createConsumer();

        TopicPartition partition1 = new TopicPartition("balance.transformer.request", 0);
        TopicPartition partition2 = new TopicPartition("balance.transformer.response", 0);
        consumer.assign(Arrays.asList(partition1, partition2));


        Map<String,Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        producer = new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new JsonSerializer<BalanceMessage>()).createProducer();

    }


    @AfterEach
    void shutdown() {
        consumer.close();
        producer.close();
    }







    private BalanceMessage createBalanceMessage() {
        return new BalanceMessage(123456, 123, "", false);
    }






}
