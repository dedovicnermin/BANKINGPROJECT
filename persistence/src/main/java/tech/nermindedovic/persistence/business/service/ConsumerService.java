package tech.nermindedovic.persistence.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;


import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import tech.nermindedovic.persistence.business.components.MsgProcessor;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;
import tech.nermindedovic.persistence.kafka.PersistenceTopicNames;

import javax.validation.constraints.NotNull;
import java.util.Optional;


@Service
@Slf4j
public class ConsumerService {


    // == dependency ==
    private final MsgProcessor processor;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    // == constructor ==
    public ConsumerService(MsgProcessor msgProcessor, KafkaTemplate<String, String> template) {
        this.processor = msgProcessor;
        this.stringKafkaTemplate = template;
    }



    /**
     * Balance request consumer
     * @param xml deserialized string
     * @return  reply back to transformer application
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = PersistenceTopicNames.INBOUND_BALANCE_REQUEST, groupId = "persistence")
    @SendTo
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

    /**
     * PRECONDITION: producer has sent an XML message for a funds transfer request
     * POSTCONDITION: producer commits transaction
     * @param xml
     */
    @KafkaListener(topics = PersistenceTopicNames.INBOUND_TRANSFER_REQUEST, groupId = "persistence", containerFactory = "nonReplying_ListenerContainerFactory")
    public void handleFundsTransferRequest(@NotNull final String xml) {
        Optional<String> errors = Optional.empty();
        log.info(xml);
        try {
            processor.processTransferRequest(xml);
        } catch (InvalidTransferMessageException e) {
            errors = Optional.of(e.getMessage() + " --- " + xml);
        }

        errors.ifPresent(this::produceErrorMessage);
    }


    private void produceErrorMessage(String errorMessage) {
        log.info("producing error message to funds.transfer.error");
        stringKafkaTemplate.send(PersistenceTopicNames.OUTBOUND_TRANSFER_ERRORS, errorMessage);
    }



}
