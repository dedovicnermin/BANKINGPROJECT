package tech.nermindedovic.persistence.business.service.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import tech.nermindedovic.persistence.business.doman.Creditor;
import tech.nermindedovic.persistence.business.doman.Debtor;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;
import tech.nermindedovic.persistence.kafka.PersistenceTopicNames;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {PersistenceTopicNames.INBOUND_TRANSFER_REQUEST, PersistenceTopicNames.OUTBOUND_TRANSFER_ERRORS})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaTransferIntegrationTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired AccountRepository accountRepository;
    @Autowired TransactionRepository transactionRepository;



    private BlockingQueue<ConsumerRecord<String, String>> error_records;
    private KafkaMessageListenerContainer<String, String> error_container;
    private Producer<String, String> producer;

    XmlMapper mapper = new XmlMapper();

    @BeforeEach
    void setup() {
        Map<String, Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("test", "true", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(PersistenceTopicNames.OUTBOUND_TRANSFER_ERRORS);
        error_container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        error_records = new LinkedBlockingQueue<>();
        error_container.setupMessageListener((MessageListener<String, String>) error_records::add);
        error_container.start();
        ContainerTestUtils.waitForAssignment(error_container, embeddedKafkaBroker.getPartitionsPerTopic());


        Map<String, Object> producerConfigs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producer = new DefaultKafkaProducerFactory<>(producerConfigs, new StringSerializer(), new StringSerializer()).createProducer();


    }


    @AfterEach
    void shutdown() {
        error_container.stop();
        producer.close();
    }


    /**
     * consumer successfully listens, passes xml to be processed/persisted, balances are updated
     */
    @Test
    void onValidTransfer_willConsumeAndPersist() throws JsonProcessingException, InterruptedException {
        accountRepository.save(new Account(11,11,"Ben", BigDecimal.TEN));
        accountRepository.save(new Account(22,22,"Ken", BigDecimal.TEN));


        TransferMessage transferMessage = new TransferMessage(100, new Creditor(11, 11), new Debtor(22,22), LocalDate.now(), BigDecimal.ONE, "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);

        producer.send(new ProducerRecord<>(PersistenceTopicNames.INBOUND_TRANSFER_REQUEST, xml));
        producer.flush();

        //check error consumer is empty
        ConsumerRecord<String, String> potentialError = error_records.poll(1000, TimeUnit.MILLISECONDS);
        assertThat(potentialError).isNull();


        // check transaction was persisted without generating id
        assertThat(transactionRepository.findById(100L)).isNotEmpty();
        // check balances of users to ensure they have been updated



        assertThat(accountRepository.findById(11L).get().getAccountBalance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(accountRepository.findById(22L).get().getAccountBalance()).isEqualTo(new BigDecimal("9.00"));
    }


    /**
     * invalid accounts raise errors, handled - sending to error topic
     */
    @Test
    void givenInvalidAccount_sendsToErrorTopic() throws JsonProcessingException, InterruptedException {
        TransferMessage transferMessage = new TransferMessage(100L, new Creditor(100L, 100L), new Debtor(1L,1L), LocalDate.now(), new BigDecimal("1.00"), "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);

        producer.send(new ProducerRecord<>(PersistenceTopicNames.INBOUND_TRANSFER_REQUEST, xml));
        producer.flush();

        //check error consumer is not empty
        ConsumerRecord<String, String> potentialError = error_records.poll(1000, TimeUnit.MILLISECONDS);
        assertThat(potentialError).isNotNull();
        assertThat(potentialError.value()).isEqualTo("PERSISTENCE --- Both accounts are not users of this bank.");

    }


    /**
     * Can handle deserialization issues
     */
    @Test
    void givenUnserializableTransferMessage_willHandleAndSendToErrorTopic() throws InterruptedException {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, Long> myProducer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new LongSerializer()).createProducer();

        myProducer.send(new ProducerRecord<>(PersistenceTopicNames.INBOUND_TRANSFER_REQUEST, 20L));
        myProducer.flush();

        //check error consumer is not empty
        ConsumerRecord<String, String> potentialError = error_records.poll(1000, TimeUnit.MILLISECONDS);
        assertThat(potentialError).isNotNull();
        assertThat(potentialError.value()).isEqualTo("PERSISTENCE --- Unable to bind XML to TransferMessagePOJO");
    }



}
