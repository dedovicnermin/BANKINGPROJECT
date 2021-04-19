package tech.nermindedovic.persistence.business.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import tech.nermindedovic.persistence.kafka.PersistenceTopicNames;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {PersistenceTopicNames.INBOUND_BALANCE_REQUEST, KafkaBalanceIntegrationTest.OUTBOUND_BALANCE})
@DirtiesContext
class KafkaBalanceIntegrationTest {

    public static final String OUTBOUND_BALANCE = "balance.update.response";

    XmlMapper mapper = new XmlMapper();

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired AccountRepository accountRepository;

    private BlockingQueue<ConsumerRecord<String, String>> records;
    private KafkaMessageListenerContainer<String, String> container;

    private Producer<String, String> producer;


    @BeforeEach
    void setup() {

        Map<String, Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("test-balance-request", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(OUTBOUND_BALANCE);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerConfigs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producer = new DefaultKafkaProducerFactory<>(producerConfigs, new StringSerializer(), new StringSerializer()).createProducer();


    }

    @AfterEach
    void shutdown() {
        container.stop();
        producer.close();
    }


    @Test
    void test_balanceMessages_WillBeConsumedAndProduced() throws JsonProcessingException, InterruptedException {

        accountRepository.save(new Account(11,11,"Ben", BigDecimal.TEN));

        BalanceMessage balanceMessage = new BalanceMessage(11, 11, "", false);
        String balanceMessageXML = mapper.writeValueAsString(balanceMessage);

        ProducerRecord<String, String> record = new ProducerRecord<>(PersistenceTopicNames.INBOUND_BALANCE_REQUEST, balanceMessageXML);
        record.headers().add(KafkaHeaders.REPLY_TOPIC, OUTBOUND_BALANCE.getBytes());

        producer.send(record);
        producer.flush();

        ConsumerRecord<String, String> consumed = records.poll(1000, TimeUnit.MILLISECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(mapper.writeValueAsString(new BalanceMessage(11,11,"10.00", false)));
    }


    @Test
    void test_balanceMessages_willReplyWithGenericBalanceMessage_whenAccountNonExistent() throws JsonProcessingException, InterruptedException {
        BalanceMessage balanceMessage = new BalanceMessage(0, 0, "", false);
        String balanceMessageXML = mapper.writeValueAsString(balanceMessage);
        ProducerRecord<String, String> record = new ProducerRecord<>(PersistenceTopicNames.INBOUND_BALANCE_REQUEST, balanceMessageXML);
        record.headers().add(KafkaHeaders.REPLY_TOPIC, OUTBOUND_BALANCE.getBytes());

        producer.send(record);
        producer.flush();

        ConsumerRecord<String, String> consumed = records.poll(1000, TimeUnit.MILLISECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo("<BalanceMessage><accountNumber>0</accountNumber><routingNumber>0</routingNumber><balance></balance><errors>true</errors></BalanceMessage>");
    }












}