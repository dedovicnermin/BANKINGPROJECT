package tech.nermindedovic.transformer_streams.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
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
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"spring.autoconfigure.exclude="
        + "org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}",
})
@EmbeddedKafka(partitions = 1, topics = {ProcessingErrorsTest.TRANSFER_INBOUND, ProcessingErrorsTest.TRANSFER_OUTBOUND, ProcessingErrorsTest.BALANCE_LEG1_INBOUND, ProcessingErrorsTest.BALANCE_LEG1_OUTBOUND, ProcessingErrorsTest.BALANCE_LEG2_INBOUND, ProcessingErrorsTest.BALANCE_LEG2_OUTBOUND})
@DirtiesContext
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessingErrorsTest {
    public static final String TRANSFER_INBOUND = "funds.transformer.request";
    public static final String TRANSFER_OUTBOUND = "funds.transfer.request";
    public static final String BALANCE_LEG1_INBOUND = "balance.transformer.request";
    public static final String BALANCE_LEG1_OUTBOUND = "balance.update.request";

    public static final String BALANCE_LEG2_INBOUND = "balance.update.response";
    public static final String BALANCE_LEG2_OUTBOUND = "balance.transformer.response";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;




    @SpyBean
    XmlMapper mapper;



    @Test
    void processTransfer_jsonProcessingException() throws JsonProcessingException, InterruptedException {
        long messageId = 999;
        Creditor creditor = new Creditor(122, 111);
        Debtor debtor = new Debtor(211, 222);
        TransferMessage transferMessage = new TransferMessage(messageId, creditor, debtor, LocalDate.now(), BigDecimal.TEN, "A memo");

        when(mapper.writeValueAsString(transferMessage)).thenThrow(JsonProcessingException.class);

        String expected = transferMessage.toString();


        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("transferMessageTest_jsonError", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(TRANSFER_OUTBOUND);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.setBeanName("transferMessageTest_jsonSerializationError");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, TransferMessage> pf = new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new JsonSerializer<>());
        KafkaTemplate<String, TransferMessage> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(TRANSFER_INBOUND);
        template.sendDefault(transferMessage);
        template.flush();

        ConsumerRecord<String, String> record = records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(expected);

        container.stop(false);
    }




    @Test
    void processBalanceLeg1_whenThrowsJsonProcessingException_returnsToString() throws InterruptedException, JsonProcessingException {
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
        String expected = balanceMessage.toString();
        when(mapper.writeValueAsString(balanceMessage)).thenThrow(JsonProcessingException.class);


        template.sendDefault(balanceMessage);
        template.flush();

        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(expected);

        container.stop(false);
    }




    @Test
    void processBalanceLeg2_whenThrowsJsonProcessingException_returnsNull() throws JsonProcessingException, InterruptedException {
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

        when(mapper.readValue(inboundXML, BalanceMessage.class)).thenThrow(JsonProcessingException.class);

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer());
        KafkaTemplate<String, String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(BALANCE_LEG2_INBOUND);
        template.sendDefault(inboundXML);
        template.flush();


        ConsumerRecord<String, BalanceMessage> consumerRecord = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumerRecord).isNotNull();
        assertThat(consumerRecord.value()).isNull();

        container.stop(false);
    }











}
