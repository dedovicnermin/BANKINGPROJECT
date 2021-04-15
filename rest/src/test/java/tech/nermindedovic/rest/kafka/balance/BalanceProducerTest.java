package tech.nermindedovic.rest.kafka.balance;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.nermindedovic.rest.business.domain.BalanceMessage;


import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;


@ExtendWith({MockitoExtension.class, SpringExtension.class})
@DirtiesContext
class BalanceProducerTest {

    @Mock
    ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> replyingKafkaTemplate;

    @Mock
    RequestReplyFuture<String, BalanceMessage, BalanceMessage> requestReplyFuture;

    @InjectMocks
    BalanceProducer balanceProducer;


    @Test
    void testGoodBalanceMessage() throws ExecutionException, InterruptedException {
        balanceProducer = new BalanceProducer(replyingKafkaTemplate);

        BalanceMessage balanceMessage = createBalanceMessage(111111,121122112);

        when(replyingKafkaTemplate.sendAndReceive(isA(ProducerRecord.class))).thenReturn(requestReplyFuture);
        when(requestReplyFuture.get()).thenReturn(new ConsumerRecord<>("balance.transformer.response", 1, 11, null, balanceMessage));

        assertDoesNotThrow(() -> balanceProducer.sendAndReceive(balanceMessage));
    }




    @ParameterizedTest
    @ValueSource(classes = {InterruptedException.class, ExecutionException.class})
    void testBalanceMessage_willThrowOnNetworkIssues(Class<Exception> exceptionClass) throws ExecutionException, InterruptedException {
        //given
        balanceProducer = new BalanceProducer(replyingKafkaTemplate);
        BalanceMessage balanceMessage = createBalanceMessage(2222,22221111);

        //when
        when(replyingKafkaTemplate.sendAndReceive(isA(ProducerRecord.class))).thenReturn(requestReplyFuture);
        when(requestReplyFuture.get()).thenThrow(exceptionClass);


        //then
        assertThrows(exceptionClass, () -> balanceProducer.sendAndReceive(balanceMessage));
    }



    private BalanceMessage createBalanceMessage(long accountNumber, long routingNumber) {
        return new BalanceMessage(accountNumber,routingNumber,"",false);
    }

}