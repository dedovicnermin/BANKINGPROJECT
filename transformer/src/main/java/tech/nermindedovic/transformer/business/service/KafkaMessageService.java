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
    private MessageTransformer transformer;
    private ReplyingKafkaTemplate<String, String, String> template;

    // == constructor ==
    public KafkaMessageService(final MessageTransformer messageTransformer, ReplyingKafkaTemplate<String, String, String> kafkaTemplate) {transformer = messageTransformer; template = kafkaTemplate;}


    // == balance request listener

    @KafkaListener(topics = "balance.transformer.request", containerFactory = "concurrentKafkaListenerContainerFactory")
    @SendTo
    public BalanceMessage listen(BalanceMessage balanceMessage) throws JsonProcessingException, InterruptedException {

        try {
            String xml = transformer.balancePojoToXML(balanceMessage);
            ProducerRecord<String, String> record = new ProducerRecord<>("balance.update.request", xml);
            record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "balance.update.response".getBytes()));

            RequestReplyFuture<String, String, String> sendAndReceive = template.sendAndReceive(record);

            //get the consumer record
            ConsumerRecord<String, String> consumerRecord = sendAndReceive.get();

            return transformer.balanceXMLToPojo(consumerRecord.value());

        } catch (ExecutionException e) {
            e.printStackTrace();
            return balanceMessage;
        }


    }














}
