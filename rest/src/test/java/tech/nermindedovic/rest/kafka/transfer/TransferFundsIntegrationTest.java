package tech.nermindedovic.rest.kafka.transfer;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import tech.nermindedovic.rest.api.RestAPI;
import tech.nermindedovic.rest.business.domain.Creditor;
import tech.nermindedovic.rest.business.domain.Debtor;
import tech.nermindedovic.rest.business.domain.TransferMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {TransferFundsIntegrationTest.OUTBOUND_TOPIC })
class TransferFundsIntegrationTest {

    public static final String OUTBOUND_TOPIC = "funds.transformer.request";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    RestAPI restAPI;

    BlockingQueue<ConsumerRecord<String, TransferMessage>> consumed;
    KafkaMessageListenerContainer<String, TransferMessage> listenerContainer;


    @BeforeEach
    void setup_receivingEnd() {
        Map<String,Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("test", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, TransferMessage> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new JsonDeserializer<>(TransferMessage.class));
        ContainerProperties containerProperties = new ContainerProperties(OUTBOUND_TOPIC);
        listenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        consumed = new LinkedBlockingQueue<>();

        listenerContainer.setupMessageListener((MessageListener<String, TransferMessage>) consumed::add);
        listenerContainer.start();
        ContainerTestUtils.waitForAssignment(listenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterEach
    void shutdown() {
        listenerContainer.stop();
    }



    @Test
    void givenValidTransferMessage_willProduceToTransformerTopic() throws ExecutionException, InterruptedException {

        TransferMessage transferMessage = TransferMessage.builder()
                .message_id(0)
                .creditor(new Creditor(1111, 1111))
                .debtor(new Debtor(2222,2222))
                .amount(BigDecimal.TEN)
                .memo("Message sent from rest application")
                .date(LocalDate.now())
                .build();


        restAPI.fundsTransferRequest(transferMessage);
        ConsumerRecord<String, TransferMessage> consumerRecord = consumed.poll(100, TimeUnit.MILLISECONDS);
        assertThat(consumerRecord).isNotNull();
        assertThat(consumerRecord.value()).isEqualTo(transferMessage);
    }






}
