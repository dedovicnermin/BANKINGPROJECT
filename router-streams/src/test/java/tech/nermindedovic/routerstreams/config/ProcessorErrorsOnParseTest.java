package tech.nermindedovic.routerstreams.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.nermindedovic.routerstreams.utils.RouterAppUtils;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(properties = {"spring.autoconfigure.exclude="
        + "org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}",
})
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@EmbeddedKafka(partitions = 1, topics = {RouterTopicNames.OUTBOUND_BALANCE_RETURN_TOPIC, RouterTopicNames.OUTBOUND_TRANSFER_ERROR, RouterTopicNames.INBOUND_INITIAL_TRANSFER_TOPIC})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessorErrorsOnParseTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeAll
    void configure() {
        System.setProperty("spring.kafka.bootstrap-servers", embeddedKafkaBroker.getBrokersAsString());
    }

    @AfterAll
    void shutdown() {
        embeddedKafkaBroker.destroy();
    }

    @Spy
    SAXBuilder saxBuilder;

    final String balanceMessageXML = "<BalanceMessage><accountNumber>200040</accountNumber><routingNumber>3452</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";

    @Test
    void onIncomingBalanceRequest_whenExceptionIsThrown_willDirectToError() throws InterruptedException, JDOMException, IOException {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-balance-errors", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(RouterTopicNames.OUTBOUND_BALANCE_RETURN_TOPIC);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);

        container.setBeanName("balanceTest-errors");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());


        when(saxBuilder.build(new ByteArrayInputStream(balanceMessageXML.getBytes()))).thenThrow(JDOMException.class);

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("balance.update.request");
        template.sendDefault(balanceMessageXML);
        template.flush();


        ConsumerRecord<String, String> record = records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(RouterAppUtils.BALANCE_ERROR_XML);

        container.stop();
    }



    @Test
    void onIncomingInitialTransfer_whenParseExceptionThrown_willDirectToErrorTopic() throws InterruptedException {
        String invalidTransfer = "<TransferMessage>Invalid";

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-transfer-parseError", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(RouterTopicNames.OUTBOUND_TRANSFER_ERROR);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);

        container.setBeanName("errorTests-parseError");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());




        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(RouterTopicNames.INBOUND_INITIAL_TRANSFER_TOPIC);
        template.sendDefault(invalidTransfer);
        template.flush();


        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).containsSequence(invalidTransfer);

        container.stop();
    }

}
