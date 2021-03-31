package tech.nermindedovic.rest.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.nermindedovic.rest.business.domain.BalanceMessage;
import tech.nermindedovic.rest.business.domain.TransferMessage;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
public class RestAPI {

    AtomicInteger counter = new AtomicInteger(1);
    private final ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> replyingKafkaTemplate;
    private final KafkaTemplate<String, TransferMessage> transferMessageTemplate;

    public RestAPI(ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> replyingKafkaTemplate, @Qualifier("transferTemplate")KafkaTemplate<String, TransferMessage> transferMessageKafkaTemplate) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.transferMessageTemplate = transferMessageKafkaTemplate;
    }


    @PostMapping(value = "balance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BalanceMessage getBalanceUpdate(@RequestBody BalanceMessage balanceMessage) throws ExecutionException, InterruptedException {


        ProducerRecord<String,BalanceMessage> record = new ProducerRecord<>("balance.transformer.request", balanceMessage);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "balance.transformer.response".getBytes()));

        RequestReplyFuture<String, BalanceMessage, BalanceMessage> sendAndReceive = replyingKafkaTemplate.sendAndReceive(record);

        ConsumerRecord<String, BalanceMessage> consumerRecord = sendAndReceive.get();
        BalanceMessage val = consumerRecord.value();

        return val;
//        return consumerRecord.value();


    }


    @PostMapping(value = "funds/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String fundsTransferRequest(@RequestBody TransferMessage transferMessage)  {

        ProducerRecord<String, TransferMessage> record = new ProducerRecord<>("funds.transformer.request", transferMessage);

        try {
            SendResult<String, TransferMessage> sendResult = transferMessageTemplate.send(record).get();
            log.info(sendResult.toString());
            return "Successfully transferred funds.";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "ERROR";
        }


    }







    public int getCounterValue() {
        return counter.get();
    }


//        record.headers().add(new RecordHeader(KafkaHeaders.CORRELATION_ID, ByteBuffer.allocateDirect(getCounterValue())));
}
