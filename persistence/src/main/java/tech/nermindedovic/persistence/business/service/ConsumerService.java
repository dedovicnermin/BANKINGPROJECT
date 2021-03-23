package tech.nermindedovic.persistence.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.istack.NotNull;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import tech.nermindedovic.persistence.business.components.MsgProcessor;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;



@Service
public class ConsumerService {
    // == dependency ==
    private final MsgProcessor processor;

    // == dependency ==
    public ConsumerService(MsgProcessor msgProcessor ) {
        this.processor = msgProcessor;
    }


    @KafkaListener(topics = "${topic.balance.request}", groupId = "${kafka.groupid}")
    @SendTo
    public String handleBalanceRequest(@NotNull final String xml) throws JsonProcessingException {
        String response = null;
        try {

            response = processor.processBalanceRequest(xml);

        } catch (JsonProcessingException e) {

            e.printStackTrace();
            BalanceMessage balanceMessage = new BalanceMessage(0, 0, "", true);
            response = processor.processFailedAttempt(balanceMessage);

            // proposal - could add a error message field to the POJO for cleaner output on client side
        }

        return response;

    }








}
