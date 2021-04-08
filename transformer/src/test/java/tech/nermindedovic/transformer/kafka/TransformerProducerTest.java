package tech.nermindedovic.transformer.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.nermindedovic.transformer.business.service.KafkaMessageService;
import tech.nermindedovic.transformer.components.MessageTransformer;
import tech.nermindedovic.transformer.pojos.BalanceMessage;
import tech.nermindedovic.transformer.pojos.Creditor;
import tech.nermindedovic.transformer.pojos.Debtor;
import tech.nermindedovic.transformer.pojos.TransferMessage;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@EmbeddedKafka(topics = {"funds.transfer.request", "funds.transfer.error"}, partitions = 1, ports = 9092)
@DirtiesContext
class TransformerProducerTest {

    @Autowired
    KafkaMessageService kafkaMessageService;





    private Consumer<String, String> consumer;


    @BeforeEach
    void setConsumer() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer = new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), new StringDeserializer()).createConsumer();

        TopicPartition partition1 = new TopicPartition("funds.transfer.request", 0);
        TopicPartition partition2 = new TopicPartition("funds.transfer.error", 0);
        consumer.assign(Arrays.asList(partition1, partition2));
    }

    @AfterEach
    void shutdown() {
        consumer.close();
    }

    @Test
    void onValidTransferMessage_willSendXmlToBroker() throws JsonProcessingException {
        TransferMessage transferMessage = createTransferMessage();
        kafkaMessageService.listen(transferMessage);
        assertThat(consumer.poll(Duration.ofMillis(3000))).hasSize(1);
    }





    private TransferMessage createTransferMessage() {
        Creditor creditor = new Creditor(5555,5555555);
        Debtor debtor = new Debtor(88888,8888888);
        return new TransferMessage(11111, creditor, debtor, new Date(), BigDecimal.TEN, "Generic memo");
    }


}