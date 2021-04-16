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


    // == constructor ==
    public ConsumerService(MsgProcessor msgProcessor) {
        this.processor = msgProcessor;
    }

    /**
     * Balance request consumer
     * @param xml deserialized string
     * @return  reply back to transformer application
     *
     */
    @KafkaListener(topics = PersistenceTopicNames.INBOUND_BALANCE_REQUEST, groupId = "persistence")
    @SendTo
    public String handleBalanceRequest(@NotNull final String xml) {
        return processor.processBalanceRequest(xml);
    }


    /**
     * PRECONDITION: producer has sent an XML message for a funds transfer request
     * POSTCONDITION: producer commits transaction
     * @param xml
     */
    @KafkaListener(topics = PersistenceTopicNames.INBOUND_TRANSFER_REQUEST, groupId = "persistence", containerFactory = "nonReplying_ListenerContainerFactory")
    public void handleFundsTransferRequest(@NotNull final String xml) {
        processor.processTransferRequest(xml);
    }






}
