package tech.nermindedovic.rest.api;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.http.MediaType;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import tech.nermindedovic.rest.business.domain.BalanceMessage;

import java.util.concurrent.ExecutionException;

@RestController
public class RestAPI {

    private final ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> replyingKafkaTemplate;

    public RestAPI(ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> replyingKafkaTemplate) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
    }


    @PostMapping(value = "balance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BalanceMessage getBalanceUpdate(@RequestBody BalanceMessage balanceMessage) throws ExecutionException, InterruptedException {


        ProducerRecord<String,BalanceMessage> record = new ProducerRecord<>("balance.transformer.request", balanceMessage);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "balance.transformer.response".getBytes()));

        RequestReplyFuture<String, BalanceMessage, BalanceMessage> sendAndReceive = replyingKafkaTemplate.sendAndReceive(record);

        ConsumerRecord<String, BalanceMessage> consumerRecord = sendAndReceive.get();
        BalanceMessage val = (BalanceMessage) consumerRecord.value();

        return val;
//        return consumerRecord.value();


    }




}
