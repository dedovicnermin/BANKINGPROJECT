package tech.nermindedovic.routerstreams.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
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
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

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
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = {RouterTopicNames.INBOUND_BALANCE_REQUEST_TOPIC, RouterTopicNames.OUTBOUND_BALANCE_REQUEST_PREFIX + "111", RouterTopicNames.OUTBOUND_BALANCE_REQUEST_PREFIX + "222"})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BalanceProcessorTest {

    final String balanceMessageXML_111 = "<BalanceMessage><accountNumber>200040</accountNumber><routingNumber>111</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";
    final String balanceMessageXML_222 = "<BalanceMessage><accountNumber>200040</accountNumber><routingNumber>222</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;


    @AfterAll
    void shutdown() {
        embeddedKafkaBroker.destroy();
    }

    @Test
    void onBalanceMessagesWith_111_routesToCorrectBank() throws InterruptedException {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-balance-111", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("balance.update.request.111");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) record -> {
            log.info("TEST CONSUMER :" + record);
            records.add(record);
        });

        container.setBeanName("balanceTest-111");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("balance.update.request");
        template.sendDefault(balanceMessageXML_111);
        template.flush();


        ConsumerRecord<String, String> record = records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(balanceMessageXML_111);

        container.stop();

    }


    @Test
    void onBalanceMessagesWith_222_routesToCorrectBank() throws InterruptedException {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-balance-222", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("balance.update.request.222");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) record -> {
            log.info("TEST CONSUMER :" + record);
            records.add(record);
        });

        container.setBeanName("balanceTest-222");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("balance.update.request");
        template.sendDefault(balanceMessageXML_222);
        template.flush();


        ConsumerRecord<String, String> record = records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(balanceMessageXML_222);

        container.stop();

    }

}