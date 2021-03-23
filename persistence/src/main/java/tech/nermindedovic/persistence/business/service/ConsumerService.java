package tech.nermindedovic.persistence.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import tech.nermindedovic.persistence.business.components.MsgProcessor;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;

import java.util.Base64;
import java.util.Optional;

@Service
public class ConsumerService {

    private final MsgProcessor processor;

    public ConsumerService(MsgProcessor msgProcessor ) {
        this.processor = msgProcessor;
    }

    @KafkaListener(topics = "${topic.balance.request}", groupId = "${kafka.groupid}")
    @SendTo
    public BalanceMessage handleBalanceRequest(String xml) {
        try {
            BalanceMessage response = processor.processBalanceRequest(xml);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            BalanceMessage balanceMessage = new BalanceMessage();
            balanceMessage.setErrors(true);


            // proposal - could add a error message field to the POJO for cleaner output on client side
        }
        return null;

    }








}
