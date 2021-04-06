package tech.nermindedovic.transformer.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import tech.nermindedovic.transformer.components.MessageTransformer;
import tech.nermindedovic.transformer.pojos.BalanceMessage;
import tech.nermindedovic.transformer.pojos.TransferMessage;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class KafkaMessageService {

    // == dependencies ==
    private final MessageTransformer transformer;
    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    private final KafkaTemplate<String, String> stringKafkaTemplate;



    // == fields ==
    private static final String GROUP_ID = "transformer";

    private static final String REQ_TO_PERSISTENCE_TOPIC = "balance.update.request";
    private static final String RES_FROM_PERSISTENCE_TOPIC = "balance.update.response";
    private static final String REQ_FROM_REST_TOPIC = "balance.transformer.request";




    // == constructor ==
    public KafkaMessageService(final MessageTransformer messageTransformer, final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate, @Qualifier("stringKafkaTemplate") KafkaTemplate<String, String> stringKafkaTemplate) {
        transformer = messageTransformer;
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.stringKafkaTemplate = stringKafkaTemplate;
    }


    /**
     *
     * @param balanceMessage object sent by REST application
     * @return balanceMessage response object to REST, after getting a response from Persistence application
     * @throws JsonProcessingException
     * @throws InterruptedException
     */
    @KafkaListener(topics = REQ_FROM_REST_TOPIC, containerFactory = "factory", groupId = GROUP_ID)
    @SendTo
    public BalanceMessage listen(final BalanceMessage balanceMessage) throws JsonProcessingException, InterruptedException {

        try {
            String xml = transformer.balancePojoToXML(balanceMessage);

            ProducerRecord<String, String> record = new ProducerRecord<>(REQ_TO_PERSISTENCE_TOPIC, xml);
            record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, RES_FROM_PERSISTENCE_TOPIC.getBytes()));
            RequestReplyFuture<String, String, String> sendAndReceive = replyingKafkaTemplate.sendAndReceive(record);


            ConsumerRecord<String, String> consumerRecord = sendAndReceive.get();
            return transformer.balanceXMLToPojo(consumerRecord.value());

        } catch (ExecutionException e) {
            e.printStackTrace();
            return balanceMessage;
        }


    }



    @KafkaListener(topics = "funds.transformer.request", containerFactory = "transferMessageListenerContainerFactory", groupId = GROUP_ID)
    public void listen(final TransferMessage transferMessage) {
        log.info("Message recieved: " + transferMessage.toString());
        try {
            String xml = transformer.transferPojoToXML(transferMessage);
            ListenableFuture<SendResult<String, String>> future = stringKafkaTemplate.send("funds.transfer.request", xml);
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onFailure(Throwable throwable) {
                    log.error(throwable.getMessage());
                }

                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.info("Transformer successfully sent transferMessage xml to broker");
                }
            });

        } catch (JsonProcessingException e) {
            stringKafkaTemplate.send("funds.transfer.error", "TRANSFORMER ERROR \n" + e.getMessage());
        }



    }














}
