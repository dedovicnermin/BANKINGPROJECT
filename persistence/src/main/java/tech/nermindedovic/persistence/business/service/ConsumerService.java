package tech.nermindedovic.persistence.business.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import tech.nermindedovic.persistence.business.components.MsgProcessor;
import tech.nermindedovic.persistence.kafka.PersistenceTopicNames;
import javax.validation.constraints.NotNull;


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
    @SendTo({"balance.update.response"})
    public String handleBalanceRequest(@NotNull final String xml) {
        log.info("PERSISTENCE RECIEVED: " + xml);
        return processor.processBalanceRequest(xml);
    }


    /**
     * PRECONDITION: producer has sent an XML message for a funds transfer request
     * POSTCONDITION: producer commits transaction
     * @param xml of TransferMessage
     */
    @KafkaListener(topics = PersistenceTopicNames.INBOUND_TRANSFER_REQUEST, groupId = "persistence", containerFactory = "nonReplying_ListenerContainerFactory")
    public void handleFundsTransferRequest(@NotNull final String xml) {
        processor.processTransferRequest(xml);
    }




    /**
     * PRE-CONDITION: leg1 / leg2 of router validation.
     * POST-CONDITION: will verify that account with routing number 111 is valid + send to router to continue processing
     * @param json of TransferValidation
     */
    @KafkaListener(topics = PersistenceTopicNames.INBOUND_TRANSFER_VALIDATION, groupId = "persistence")
    @SendTo(PersistenceTopicNames.OUTBOUND_ROUTER_VALIDATION)
    public String validateAccount(@NotNull final String json) {
        return processor.processTransferValidation(json);
    }



    /**
     * PRECONDITION: both accounts have been validated already by router, router has reached the final leg
     * POST-CONDITION: delegates to msg processor to be processed
     * @param xml of TransferMessage
     */
    @KafkaListener(topics = PersistenceTopicNames.INBOUND_TRANSFER_SINGLE_USER, groupId = "persistence", containerFactory = "nonReplying_ListenerContainerFactory")
    public void handleSingleUserFundsTransferRequest(@NotNull final String xml) {
        processor.processTransferRequestTwoBanks(xml);
    }
















}
