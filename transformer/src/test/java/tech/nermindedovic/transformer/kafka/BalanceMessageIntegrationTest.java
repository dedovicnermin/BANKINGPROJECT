package tech.nermindedovic.transformer.kafka;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import tech.nermindedovic.transformer.business.service.KafkaMessageService;
import tech.nermindedovic.transformer.pojos.BalanceMessage;
import tech.nermindedovic.transformer.pojos.TransferMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, ports = 8999)
@ExtendWith({SpringExtension.class})
@DirtiesContext
class BalanceMessageIntegrationTest {



    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private static final String INITIATION_TOPIC = TransformerTopicNames.INBOUND_REST_BALANCE;
    private static final String OUTBOUND_TOPIC = TransformerTopicNames.OUTBOUND_PERSISTENCE_BALANCE;
    private static final String REPLY_INBOUND_TOPIC = TransformerTopicNames.INBOUND_PERSISTENCE_BALANCE;

    @Autowired
    KafkaMessageService kafkaMessageService;
    XmlMapper mapper = new XmlMapper();
    BlockingQueue<ConsumerRecord<String, String>> records;
    KafkaMessageListenerContainer<String,String> container;
    Producer<String, String> producer;


    @BeforeEach
    void setup() {
        Map<String, Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(OUTBOUND_TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory,containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String,String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }


    @AfterEach
    void shutdown() {
        container.stop();
    }

    @Disabled("How can I simulate consuming and producing back before calling the method that does the work")
    @Test
    void onValidBalanceMessage_sendsAndRecieves_BalanceMessageSuccess() throws InterruptedException {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer()).createProducer();

        BalanceMessage balanceMessage = createBalanceMessage();
        ConsumerRecord<String, String> singleRecord = records.poll(10000, TimeUnit.MILLISECONDS);
//        producer.send()

        BalanceMessage response = kafkaMessageService.listen(balanceMessage);
        assertThat(response).isNotNull();
    }



    private BalanceMessage createBalanceMessage() {
        return new BalanceMessage(123456, 123, "", false);
    }






}
