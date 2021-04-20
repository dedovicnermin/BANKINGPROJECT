package tech.nermindedovic.transformer.kafka.transfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import tech.nermindedovic.transformer.business.pojos.Creditor;
import tech.nermindedovic.transformer.business.pojos.Debtor;
import tech.nermindedovic.transformer.business.pojos.TransferMessage;
import tech.nermindedovic.transformer.kafka.TransformerTopicNames;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {TransferMessageIntegrationTest.INBOUND_TOPIC, TransferMessageIntegrationTest.OUTBOUND_TOPIC, TransferMessageIntegrationTest.ERROR_TOPIC})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferMessageIntegrationTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    public static final String INBOUND_TOPIC = TransformerTopicNames.INBOUND_REST_TRANSFER;
    public static final String OUTBOUND_TOPIC = TransformerTopicNames.OUTBOUND_PERSISTENCE_TRANSFER;
    public static final String ERROR_TOPIC = TransformerTopicNames.OUTBOUND_TRANSFER_ERRORS;


    XmlMapper mapper = new XmlMapper();

    BlockingQueue<ConsumerRecord<String, String>> records;
    KafkaMessageListenerContainer<String,String> container;

    Producer<String, TransferMessage> producer;
    Producer<String, String> badProducer;





    /**
     * Producer will successfully produce Transfer message to persistence
     */

    @Test
    void inboundFromRest_willSendOutbound_toPersistence_test() throws InterruptedException, JsonProcessingException {
        setup_transferProducerConfig();

        producer.send(new ProducerRecord<>(INBOUND_TOPIC, createTransferMessage()));
        producer.flush();

        String xml = mapper.writeValueAsString(createTransferMessage());
        ConsumerRecord<String, String> singleRecord = records.poll(1000, TimeUnit.MILLISECONDS);
        assertThat(singleRecord).isNotNull();
        assertThat(singleRecord.value()).isEqualTo(xml);

        tearDown_transferProducerConfig();
    }


    @Test
    void listenerContainerCantDeserialize_errorHandlerSendsToErrorTopic_test() throws InterruptedException {

        setup_errorConfig();

        badProducer.send(new ProducerRecord<>(INBOUND_TOPIC, "This is NOT a transfer message. This should not make it past one line of .listen code and container will delegate to error message handler"));
        badProducer.flush();

        ConsumerRecord<String, String> record = records.poll(10000, TimeUnit.MILLISECONDS);
        assertThat(record).isNotNull();
        assertThat(record.value()).contains("Error deserializing key/value for partition funds.");


        shutdown_errorConfig();
    }







    void setup_transferProducerConfig() {
        Map<String, Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("transformer-consumerForTransferMessage", "false", embeddedKafkaBroker));
        consumerConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, "transformerTestClientId-transferMessage");
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



    void tearDown_transferProducerConfig()  {
        container.stop(true);
        producer.close();
    }



    void setup_errorConfig() {
        Map<String, Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("consumer-transferMessage-error-test", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(ERROR_TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String,String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerConfigs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        badProducer = new DefaultKafkaProducerFactory<>(producerConfigs, new StringSerializer(), new StringSerializer()).createProducer();
    }

    void shutdown_errorConfig() {
        container.stop();
        badProducer.close();
    }






    private TransferMessage createTransferMessage() {
        Creditor creditor = new Creditor(5555,5555555);
        Debtor debtor = new Debtor(88888,8888888);
        return new TransferMessage(11111, creditor, debtor, LocalDate.now(), BigDecimal.TEN, "Generic memo");
    }




}