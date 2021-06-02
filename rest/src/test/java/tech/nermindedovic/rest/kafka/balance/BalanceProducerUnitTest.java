package tech.nermindedovic.rest.kafka.balance;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import tech.nermindedovic.AvroBalanceMessage;
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.rest.Topics;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
 class BalanceProducerUnitTest {

    @Mock
    ReplyingKafkaTemplate<String, AvroBalanceMessage, BalanceMessage> replyingKafkaTemplate;
    @Mock
    RequestReplyFuture<String, AvroBalanceMessage, BalanceMessage> sendAndReceive;





    @Test
    void sendingAvro_willReturnBalanceMessage() throws ExecutionException, InterruptedException {
        final BalanceProducer balanceProducer = new BalanceProducer(replyingKafkaTemplate);
        long AN = 333, RN = 222;
        BalanceMessage input = new BalanceMessage(AN, RN, "0.00", false);
        BalanceMessage output = new BalanceMessage(AN, RN, "10.00", false);
        when(replyingKafkaTemplate.sendAndReceive(any())).thenReturn(sendAndReceive);
        when(sendAndReceive.get()).thenReturn(new ConsumerRecord<>(Topics.BALANCE_OUTBOUND, 0, 0, null, output));

        assertThat(balanceProducer.sendAndReceive(input)).isEqualTo(output);
    }

    @Test
    void givenBalanceMessage_willConvertToFormattedAvro() {
        final BalanceProducer balanceProducer = new BalanceProducer(replyingKafkaTemplate);
        long AN = 31652, RN = 111;
        BalanceMessage balanceMessage = new BalanceMessage(AN, RN, null, false);
        AvroBalanceMessage expected = new AvroBalanceMessage(AN, RN, "0.00", false);
        assertThat(balanceProducer.toAvroPojo(balanceMessage)).isEqualTo(expected);
    }




}
