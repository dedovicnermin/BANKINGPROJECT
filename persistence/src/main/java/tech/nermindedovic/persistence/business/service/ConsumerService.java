package tech.nermindedovic.persistence.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import tech.nermindedovic.persistence.business.components.MsgProcessor;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import java.util.Optional;


@Service
@Slf4j
public class ConsumerService {


    // == dependency ==
    private final MsgProcessor processor;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    // == dependency ==
    public ConsumerService(MsgProcessor msgProcessor, KafkaTemplate<String, String> template) {
        this.processor = msgProcessor;
        this.stringKafkaTemplate = template;
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
    @SendTo
    public String handleBalanceRequest(@NotNull final String xml)  {
        String response = null;
        try {
            response = processor.processBalanceRequest(xml);
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
            BalanceMessage balanceMessage = new BalanceMessage(0, 0, "", true);
            try {
                response = processor.processFailedAttempt(balanceMessage);
            } catch (JsonProcessingException jsonProcessingException) {
                jsonProcessingException.printStackTrace();
            }
        }

        log.info(response);
        return response;
    }

    /**
     * PRECONDITION: producer has sent an XML message for a funds transfer request
     * POSTCONDITION: producer commits transaction
     * @param xml
     * @return
     */
    @KafkaListener(topics = "${funds.transfer.request.topic}", groupId = "persistence", containerFactory = "nonReplying_ListenerContainerFactory")
    public void handleFundsTransferRequest(@NotNull final String xml) {
        Optional<String> errors = Optional.empty();
        log.info(xml);
        try {
            processor.processTransferRequest(xml);
        } catch (InvalidTransferMessageException e) {
            errors = Optional.of(e.getMessage());
        }

        errors.ifPresent(this::produceErrorMessage);
    }


    private void produceErrorMessage(String errorMessage) {
        log.info("producing error message to funds.transfer.error ...");
        stringKafkaTemplate.send("funds.transfer.error", errorMessage);
    }



}
