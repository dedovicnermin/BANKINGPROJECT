package tech.nermindedovic.transformer_streams.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
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
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.transformer_streams.pojos.Creditor;
import tech.nermindedovic.transformer_streams.pojos.Debtor;
import tech.nermindedovic.transformer_streams.pojos.TransferMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@EmbeddedKafka(partitions = 1, topics = {TransferMessageProcessorTest.TRANSFER_INBOUND, TransferMessageProcessorTest.TRANSFER_OUTBOUND})
@DirtiesContext
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferMessageProcessorTest {

    public static final String TRANSFER_INBOUND = "funds.transformer.request";
    public static final String TRANSFER_OUTBOUND = "funds.transfer.request";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;




    @AfterAll
    void shutDown () {
        embeddedKafkaBroker.destroy();
    }


    @SpyBean
    XmlMapper mapper;



    @Test
    @DisplayName("=== REST (TransferMessage POJO  ->  TRANSFORMER  ->  ROUTER (XML)")
    void processTransfer() throws JsonProcessingException, InterruptedException {
        TransferMessage transferMessage = new TransferMessage(123, new Creditor(844, 111), new Debtor(520, 111), LocalDate.now(), new BigDecimal("10.00"), "memo here");



        String expected = mapper.writeValueAsString(transferMessage);



        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("transferMessageTest", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(TRANSFER_OUTBOUND);
        KafkaMessageListenerContainer<String, String>  container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.setBeanName("transferMessageTest");
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

        container.stop();
    }




}