package tech.nermindedovic.persistence.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import tech.nermindedovic.persistence.business.components.MsgProcessor;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;


@Service
@Slf4j
public class ConsumerService {
    // == dependency ==
    private final MsgProcessor processor;

    // == dependency ==
    public ConsumerService(MsgProcessor msgProcessor ) {
        this.processor = msgProcessor;
    }



    /**
     * Balance request consumer
     *
     *  reply template set in configuration. Set replyTemplate on listener container factory. supplied with bean - kafkaTemplate
                                                      with factory.setReplyTemplate(KafkaTemplate)
     *
     * @param xml deserialized string coming off the queue
     * @return  returns xml string
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = "${balance.request.topic}", groupId = "persistence")
    @SendTo(value = "${balance.response.topic}")
    public String handleBalanceRequest(@NotNull final String xml) throws JsonProcessingException {
        String response = null;
        try {

            response = processor.processBalanceRequest(xml);


        } catch (JsonProcessingException e) {

            log.info(e.getMessage());
            BalanceMessage balanceMessage = new BalanceMessage(0, 0, "", true);
            response = processor.processFailedAttempt(balanceMessage);
        }

        log.info(response);
        return response;

    }






    // proposal - could add a error message field to the POJO for cleaner output on client side
    //    @SendTo(value = "${balance.request.topic}")   -> Works but ends up in endless loop
    //    @SendTo ->



}
