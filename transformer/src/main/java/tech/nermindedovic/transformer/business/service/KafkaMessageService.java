package tech.nermindedovic.transformer.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import tech.nermindedovic.transformer.components.MessageTransformer;
import tech.nermindedovic.transformer.pojos.BalanceMessage;

import java.util.concurrent.ExecutionException;


@Service
public class KafkaMessageService {

    // == dependencies ==
    private final MessageTransformer transformer;
    private final ReplyingKafkaTemplate<String, String, String> template;



    // == fields ==
    private static final String GROUP_ID = "transformer";

    private static final String REQ_TO_PERSISTENCE_TOPIC = "balance.update.request";
    private static final String RES_FROM_PERSISTENCE_TOPIC = "balance.update.response";
    private static final String REQ_FROM_REST_TOPIC = "balance.transformer.request";




    // == constructor ==
    public KafkaMessageService(final MessageTransformer messageTransformer, final ReplyingKafkaTemplate<String, String, String> kafkaTemplate) {
        transformer = messageTransformer; template = kafkaTemplate;
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
            RequestReplyFuture<String, String, String> sendAndReceive = template.sendAndReceive(record);


            ConsumerRecord<String, String> consumerRecord = sendAndReceive.get();
            return transformer.balanceXMLToPojo(consumerRecord.value());

        } catch (ExecutionException e) {
            e.printStackTrace();
            return balanceMessage;
        }


    }














}
