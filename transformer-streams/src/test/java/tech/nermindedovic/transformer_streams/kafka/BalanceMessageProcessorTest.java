package tech.nermindedovic.transformer_streams.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.transformer_streams.pojos.BalanceMessage;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest(properties = {"spring.autoconfigure.exclude="
        + "org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}",
})
@EmbeddedKafka(partitions = 1, topics = {BalanceMessageProcessorTest.BALANCE_LEG1_INBOUND, BalanceMessageProcessorTest.BALANCE_LEG1_OUTBOUND, BalanceMessageProcessorTest.BALANCE_LEG2_INBOUND, BalanceMessageProcessorTest.BALANCE_LEG2_OUTBOUND})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BalanceMessageProcessorTest {

    public static final String BALANCE_LEG1_INBOUND = "balance.transformer.request";
    public static final String BALANCE_LEG1_OUTBOUND = "balance.update.request";

    public static final String BALANCE_LEG2_INBOUND = "balance.update.response";
    public static final String BALANCE_LEG2_OUTBOUND = "balance.transformer.response";


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    private static final XmlMapper mapper = new XmlMapper();

    @AfterAll
    void shutDown () {
        embeddedKafkaBroker.destroy();
    }

    @Test
    @DisplayName("=== REST (BalanceMessage)  ->  TRANSFORMER  ->  ROUTER (XML STRING) ===")
    void processBalanceLeg1() throws JsonProcessingException, InterruptedException {

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("balance-leg1-test", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(BALANCE_LEG1_OUTBOUND);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.setBeanName("balanceLeg1Test");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, BalanceMessage> pf = new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new JsonSerializer<>());
        KafkaTemplate<String, BalanceMessage> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(BALANCE_LEG1_INBOUND);

        BalanceMessage balanceMessage = new BalanceMessage(828, 111, "", false);
        String expected = mapper.writeValueAsString(balanceMessage);


        template.sendDefault(balanceMessage);
        template.flush();

        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(expected);

        container.stop();
    }





    @Test
    @DisplayName("=== PERSISTENCE (XML String)  ->  TRANSFORMER  ->  REST (BalanceMessage POJO) ===")
    void processBalanceLeg2() throws JsonProcessingException, InterruptedException {

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("balance-leg2-test", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, BalanceMessage> cf = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new JsonDeserializer<>(BalanceMessage.class));
        ContainerProperties containerProperties = new ContainerProperties(BALANCE_LEG2_OUTBOUND);
        KafkaMessageListenerContainer<String, BalanceMessage> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, BalanceMessage>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, BalanceMessage>) records::add);
        container.setBeanName("balanceLeg2Test");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());


        BalanceMessage balanceMessage = new BalanceMessage(828, 111, "", false);
        String inboundXML = mapper.writeValueAsString(balanceMessage);

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer());
        KafkaTemplate<String, String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(BALANCE_LEG2_INBOUND);
        template.sendDefault(inboundXML);
        template.flush();


        ConsumerRecord<String, BalanceMessage> consumerRecord = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumerRecord).isNotNull();
        assertThat(consumerRecord.value()).isEqualTo(balanceMessage);

        container.stop();
    }









}