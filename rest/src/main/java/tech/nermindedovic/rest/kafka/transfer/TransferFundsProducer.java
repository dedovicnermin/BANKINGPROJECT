package tech.nermindedovic.rest.kafka.transfer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import tech.nermindedovic.rest.business.domain.TransferMessage;

@Component
@Slf4j
public class TransferFundsProducer {

    private static final String TOPIC = "funds.transformer.request";
    private final KafkaTemplate<String, TransferMessage> transferMessageTemplate;

    public TransferFundsProducer(@Qualifier("transferTemplate") KafkaTemplate<String, TransferMessage> transferMessageKafkaTemplate) {
        this.transferMessageTemplate = transferMessageKafkaTemplate;
    }

    public ListenableFuture<SendResult<String, TransferMessage>> sendTransferMessage(final TransferMessage transferMessage) {
        ProducerRecord<String, TransferMessage> record = new ProducerRecord<>(TOPIC, transferMessage);
        ListenableFuture<SendResult<String, TransferMessage>> future = transferMessageTemplate.send(record);
        future.addCallback(createTransferCallBack());
        return future;
    }


    private ListenableFutureCallback<SendResult<String, TransferMessage>> createTransferCallBack() {
        return new ListenableFutureCallback<SendResult<String, TransferMessage>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.error("Could not complete funds transfer request.\n{}", throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, TransferMessage> stringTransferMessageSendResult) {
                log.info("Message with ID {} has successfully sent, with offset ({}) and partition ({}).", stringTransferMessageSendResult.getProducerRecord().value().getMessage_id(), stringTransferMessageSendResult.getRecordMetadata().offset(), stringTransferMessageSendResult.getRecordMetadata().partition());
            }
        };
    }

}
