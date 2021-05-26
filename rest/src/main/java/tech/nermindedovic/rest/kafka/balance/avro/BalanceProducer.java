package tech.nermindedovic.rest.kafka.balance.avro;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Component;
import tech.nermindedovic.library.avro.BalanceMessage;

import java.util.concurrent.ExecutionException;

@Component
@Profile("avro")
public class BalanceProducer {

    private static final String TOPIC = "balance.transformer.request";
    private final ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> replyingKafkaTemplate;

    public BalanceProducer(ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> replyingKafkaTemplate) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
    }


    public BalanceMessage sendAndReceive(BalanceMessage balanceMessage) throws ExecutionException, InterruptedException {
        ProducerRecord<String,BalanceMessage> producerRecord = new ProducerRecord<>(TOPIC, balanceMessage);
        RequestReplyFuture<String, BalanceMessage, BalanceMessage> sendAndReceive = replyingKafkaTemplate.sendAndReceive(producerRecord);
        ConsumerRecord<String, BalanceMessage> consumerRecord = sendAndReceive.get();
        return consumerRecord.value();
    }

}
