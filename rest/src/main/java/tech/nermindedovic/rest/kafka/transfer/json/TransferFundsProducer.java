package tech.nermindedovic.rest.kafka.transfer.json;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import tech.nermindedovic.library.pojos.TransferMessage;


import javax.validation.constraints.NotNull;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@Profile("!avro")
public class TransferFundsProducer {

    private static final String TOPIC = "funds.transformer.request";
    private final KafkaTemplate<String, TransferMessage> transferMessageTemplate;

    public TransferFundsProducer(@Qualifier("transferTemplate") KafkaTemplate<String, TransferMessage> transferMessageKafkaTemplate) {
        this.transferMessageTemplate = transferMessageKafkaTemplate;
    }

    public String sendTransferMessage(@NotNull final TransferMessage transferMessage) throws ExecutionException, InterruptedException {
        ProducerRecord<String, TransferMessage> producerRecord = new ProducerRecord<>(TOPIC, transferMessage);
        ListenableFuture<SendResult<String, TransferMessage>> future = transferMessageTemplate.send(producerRecord);
        future.addCallback(createTransferCallBack());
        SendResult<String, TransferMessage> result = future.completable().get();
        long messageId = result.getProducerRecord().value().getMessageId();
        return result.getRecordMetadata().hasOffset() ? String.format("Message (%d) has been sent successfully.", messageId) : String.format("Message (%d) was not able to successfully send", messageId);

    }


    private ListenableFutureCallback<SendResult<String, TransferMessage>> createTransferCallBack() {
        return new ListenableFutureCallback<SendResult<String, TransferMessage>>() {
            @Override
            public void onFailure(Throwable throwable) { log.error("Could not complete funds transfer request.\n{}", throwable.getMessage()); }

            @Override
            public void onSuccess(SendResult<String, TransferMessage> stringTransferMessageSendResult) {
                TransferMessage transferMessage = stringTransferMessageSendResult.getProducerRecord().value();
                RecordMetadata metadata = stringTransferMessageSendResult.getRecordMetadata();
                log.info("Message with ID {} has successfully sent, with offset ({}) and partition ({}).",
                        transferMessage.getMessageId(),
                        metadata.offset(),
                        metadata.partition());
            }
        };
    }

}
