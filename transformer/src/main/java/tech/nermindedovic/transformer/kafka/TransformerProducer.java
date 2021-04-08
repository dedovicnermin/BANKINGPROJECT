package tech.nermindedovic.transformer.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import tech.nermindedovic.transformer.components.MessageTransformer;
import tech.nermindedovic.transformer.pojos.BalanceMessage;
import tech.nermindedovic.transformer.pojos.TransferMessage;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class TransformerProducer {

    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final MessageTransformer transformer;

    public TransformerProducer(final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate, @Qualifier("stringKafkaTemplate") KafkaTemplate<String, String> stringKafkaTemplate, MessageTransformer messageTransformer) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.transformer = messageTransformer;
    }


    private static final String REQ_TO_PERSISTENCE_TOPIC = "balance.update.request";
    private static final String RES_FROM_PERSISTENCE_TOPIC = "balance.update.response";



    public BalanceMessage sendAndReceiveBalanceMessage(BalanceMessage balanceMessage)  {
        try {
            String xml = transformer.balancePojoToXML(balanceMessage);
            ProducerRecord<String, String> record = new ProducerRecord<>(REQ_TO_PERSISTENCE_TOPIC, xml);
            record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, RES_FROM_PERSISTENCE_TOPIC.getBytes()));
            RequestReplyFuture<String, String, String> sendAndReceive = replyingKafkaTemplate.sendAndReceive(record);
            ConsumerRecord<String, String> consumerRecord = sendAndReceive.get();
            return transformer.balanceXMLToPojo(consumerRecord.value());
        } catch (JsonProcessingException | InterruptedException | ExecutionException exception) {
            balanceMessage.setErrors(true);
            return balanceMessage;
        }


    }


    public void sendTransferMessage(final TransferMessage transferMessage) {
        try {
            String xml = transformer.transferPojoToXML(transferMessage);
            ListenableFuture<SendResult<String, String>> future = stringKafkaTemplate.send("funds.transfer.request", xml);
            future.addCallback(createCallBack());
        } catch (JsonProcessingException e) {
            stringKafkaTemplate.send("funds.transfer.error", "TRANSFORMER ERROR \n" + e.getMessage());
        }
    }


    private ListenableFutureCallback<SendResult<String, String>> createCallBack() {
        return new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.error(throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info("Transformer successfully sent transferMessage xml to broker");
            }
        };
    }



}
