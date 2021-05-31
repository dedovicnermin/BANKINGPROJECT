package tech.nermindedovic.rest.kafka.transfer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import tech.nermindedovic.AvroCreditor;
import tech.nermindedovic.AvroDebtor;
import tech.nermindedovic.AvroTransferMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;


import javax.validation.constraints.NotNull;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class TransferFundsProducer {

    private static final String TOPIC = "funds.transformer.request";
    private final KafkaTemplate<String, AvroTransferMessage> transferMessageTemplate;

    public TransferFundsProducer(@Qualifier("transferTemplate") KafkaTemplate<String, AvroTransferMessage> transferMessageKafkaTemplate) {
        this.transferMessageTemplate = transferMessageKafkaTemplate;
    }

    public String sendTransferMessage(@NotNull final TransferMessage transferMessage) throws ExecutionException, InterruptedException {
        ProducerRecord<String, AvroTransferMessage> producerRecord = new ProducerRecord<>(TOPIC, toAvro(transferMessage));
        ListenableFuture<SendResult<String, AvroTransferMessage>> future = transferMessageTemplate.send(producerRecord);
        future.addCallback(createTransferCallBack());
        SendResult<String, AvroTransferMessage> result = future.get();
        long messageId = transferMessage.getMessageId();
        return result.getRecordMetadata().hasOffset() ? String.format("Message (%d) has been sent successfully.", messageId) : String.format("Message (%d) was not able to successfully send", messageId);
    }


    /**
     * For logging purposes.
     * @return callback handling broker response logic
     */
    private ListenableFutureCallback<SendResult<String, AvroTransferMessage>> createTransferCallBack() {
        return new ListenableFutureCallback<SendResult<String, AvroTransferMessage>>() {
            @Override
            public void onFailure(Throwable throwable) { log.error("Could not complete funds transfer request.\n{}", throwable.getMessage()); }

            @Override
            public void onSuccess(SendResult<String, AvroTransferMessage> stringTransferMessageSendResult) {
                AvroTransferMessage transferMessage = stringTransferMessageSendResult.getProducerRecord().value();
                RecordMetadata metadata = stringTransferMessageSendResult.getRecordMetadata();
                log.info("Message with ID {} has successfully sent, with offset ({}) and partition ({}).",
                        transferMessage.getMessageId(),
                        metadata.offset(),
                        metadata.partition());
            }
        };
    }

    public AvroTransferMessage toAvro(TransferMessage pojo) {
        Creditor pojoCreditor = pojo.getCreditor();
        AvroCreditor avroCreditor = new AvroCreditor(pojoCreditor.getAccountNumber(), pojoCreditor.getRoutingNumber());

        Debtor pojoDebtor = pojo.getDebtor();
        AvroDebtor avroDebtor = new AvroDebtor(pojoDebtor.getAccountNumber(), pojoDebtor.getRoutingNumber());

        return new AvroTransferMessage(pojo.getMessageId(), avroCreditor, avroDebtor, pojo.getDate().toString(), pojo.getAmount().toString(), pojo.getMemo());
    }

}
