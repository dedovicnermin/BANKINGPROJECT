package tech.nermindedovic.rest.kafka.balance;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import tech.nermindedovic.rest.business.domain.BalanceMessage;

import java.util.concurrent.ExecutionException;

@Component
public class BalanceProducer {

    private static final String TOPIC = "balance.transformer.request";
    private static final String RESP_TOPIC = "balance.transformer.response";
    private final ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> replyingKafkaTemplate;

    public BalanceProducer(ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> replyingKafkaTemplate) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
    }


    public BalanceMessage sendAndReceive(BalanceMessage balanceMessage) throws ExecutionException, InterruptedException {
        ProducerRecord<String,BalanceMessage> record = new ProducerRecord<>(TOPIC, balanceMessage);
//        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, RESP_TOPIC.getBytes()));
        RequestReplyFuture<String, BalanceMessage, BalanceMessage> sendAndReceive = replyingKafkaTemplate.sendAndReceive(record);
        ConsumerRecord<String, BalanceMessage> consumerRecord = sendAndReceive.get();
        return consumerRecord.value();
    }

}
