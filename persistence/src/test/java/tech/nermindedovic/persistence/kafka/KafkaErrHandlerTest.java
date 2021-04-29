package tech.nermindedovic.persistence.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class KafkaErrHandlerTest {

    @Mock
    KafkaTemplate<String, String> mockTemplate;

    @Mock
    List<ConsumerRecord<?, ?>> mockListOfConsumerRecords;

    @Mock
    Consumer<?, ?> mockConsumer;

    @Mock
    MessageListenerContainer mockMessageListenerContainer;

    @Mock
    ConsumerRecord<?, ?> mockConsumerRecord;

    @InjectMocks
    KafkaErrHandler kafkaErrHandler;



    @Test
    void handleJustLogs() {
        KafkaErrHandler kafkaErrHandler = new KafkaErrHandler(mockTemplate);
        assertDoesNotThrow(() -> kafkaErrHandler.handle(new Exception(), mockConsumerRecord));
    }

    @Test
    void testHandle() {
        KafkaErrHandler kafkaErrHandler = new KafkaErrHandler(mockTemplate);
        assertDoesNotThrow(() -> kafkaErrHandler.handle(new SerializationException("unable to deserialize"), mockConsumerRecord, mockConsumer));
    }

    @Test
    void testHandle1() {
        kafkaErrHandler = new KafkaErrHandler(mockTemplate);
        SerializationException serializationException = new SerializationException("Error deserializing key/value for partition funds.transfer.request.111-1 at offset 339798. If needed, please seek past the record to continue consumption.");
        assertDoesNotThrow(() -> kafkaErrHandler.handle(serializationException, mockListOfConsumerRecords, mockConsumer, mockMessageListenerContainer));

    }
}