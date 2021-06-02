package tech.nermindedovic.rest.kafka.balance;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Component;
import tech.nermindedovic.AvroBalanceMessage;
import tech.nermindedovic.library.pojos.BalanceMessage;


import java.util.concurrent.ExecutionException;

@Component
public class BalanceProducer {

    private static final String TOPIC = "balance.transformer.request";
    private final ReplyingKafkaTemplate<String, AvroBalanceMessage, BalanceMessage> replyingKafkaTemplate;

    public BalanceProducer(ReplyingKafkaTemplate<String, AvroBalanceMessage, BalanceMessage> replyingKafkaTemplate) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
    }


    public BalanceMessage sendAndReceive(BalanceMessage balanceMessage) throws ExecutionException, InterruptedException {
        ProducerRecord<String,AvroBalanceMessage> producerRecord = new ProducerRecord<>(TOPIC, toAvroPojo(balanceMessage));
        RequestReplyFuture<String, AvroBalanceMessage, BalanceMessage> sendAndReceive = replyingKafkaTemplate.sendAndReceive(producerRecord);
        ConsumerRecord<String, BalanceMessage> consumerRecord = sendAndReceive.get();
        return consumerRecord.value();
    }

    public AvroBalanceMessage toAvroPojo(BalanceMessage pojo) {
        return new AvroBalanceMessage(pojo.getAccountNumber(), pojo.getRoutingNumber(), "0.00", false);
    }

}
