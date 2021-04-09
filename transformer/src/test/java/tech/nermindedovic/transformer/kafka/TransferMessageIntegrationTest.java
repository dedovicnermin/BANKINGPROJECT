package tech.nermindedovic.transformer.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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

import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import tech.nermindedovic.transformer.pojos.Creditor;
import tech.nermindedovic.transformer.pojos.Debtor;
import tech.nermindedovic.transformer.pojos.TransferMessage;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1)
@ExtendWith({SpringExtension.class})
@DirtiesContext
class TransferMessageIntegrationTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private static final String INBOUND_TOPIC = TransformerTopicNames.INBOUND_REST_TRANSFER;
    private static final String OUTBOUND_TOPIC = TransformerTopicNames.OUTBOUND_PERSISTENCE_TRANSFER;

    XmlMapper mapper = new XmlMapper();

    BlockingQueue<ConsumerRecord<String, String>> records;
    KafkaMessageListenerContainer<String,String> container;

    Producer<String, TransferMessage> producer;

    @BeforeEach
    void setup() {
        Map<String, Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(OUTBOUND_TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String,String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());


        Map<String, Object> producerConfigs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        JsonSerializer<TransferMessage> transferMessageJsonSerializer = new JsonSerializer<>();
        producer = new DefaultKafkaProducerFactory<>(producerConfigs, new StringSerializer(), transferMessageJsonSerializer).createProducer();

    }


    @AfterEach
    void tearDown() {
        container.stop();
        producer.close();
    }




    @Test
    void inboundFromRest_willSendOutbound_toPersistence_test() throws InterruptedException, JsonProcessingException {

        producer.send(new ProducerRecord<>(INBOUND_TOPIC, createTransferMessage()));
        producer.flush();


        String xml = mapper.writeValueAsString(createTransferMessage());
        ConsumerRecord<String, String> singleRecord = records.poll(1000, TimeUnit.MILLISECONDS);
        assertThat(singleRecord).isNotNull();
        assertThat(singleRecord.value()).isEqualTo(xml);
    }






    private TransferMessage createTransferMessage() {
        Creditor creditor = new Creditor(5555,5555555);
        Debtor debtor = new Debtor(88888,8888888);
        return new TransferMessage(11111, creditor, debtor, new Date(), BigDecimal.TEN, "Generic memo");
    }




}