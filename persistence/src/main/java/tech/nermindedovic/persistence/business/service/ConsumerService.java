package tech.nermindedovic.persistence.business.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import tech.nermindedovic.library.pojos.TransferValidation;
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

    @KafkaListener(topics = PersistenceTopicNames.INBOUND_BALANCE_REQUEST, groupId = "${spring.kafka.consumer.groupId}")
    @SendTo(PersistenceTopicNames.OUTBOUND_BALANCE_RESPONSE)
    public String handleBalanceRequest(@NotNull final String xml) {
        return processor.processBalanceRequest(xml);
    }


    /**
     * PRECONDITION: producer has sent an XML message for a funds transfer request
     * POST-CONDITION: producer commits transaction
     * @param transferRecord of key: messageId , value: TransferMessage
     */
    @KafkaListener(topics = PersistenceTopicNames.INBOUND_TRANSFER_REQUEST, groupId = "${spring.kafka.consumer.groupId}", containerFactory = "nonReplying_ListenerContainerFactory")
    public void handleFundsTransferRequest(@NotNull ConsumerRecord<String, String> transferRecord) {
        processor.processTransferRequest(transferRecord.key(),transferRecord.value());
    }




    /**
     * PRE-CONDITION: leg1 / leg2 of router validation.
     * @param validation : TransferValidation
     */
    @KafkaListener(topics = PersistenceTopicNames.INBOUND_TRANSFER_VALIDATION, groupId = "${spring.kafka.consumer.groupId}", containerFactory = "validationListenerContainerFactory")
//    @SendTo(PersistenceTopicNames.OUTBOUND_ROUTER_VALIDATION)
    public void validateAccount(@NotNull ConsumerRecord<String, TransferValidation> validation) {
        log.info("RECEIVED VALIDATION: " + validation.value());
        processor.processTransferValidation(validation.key(), validation.value());
    }



    /**
     * PRECONDITION: both accounts have been validated already by router, router has reached the final leg.
     * POST-CONDITION: delegates to msg processor to be processed
     * @param xml of TransferMessage
     */
    @KafkaListener(topics = PersistenceTopicNames.INBOUND_TRANSFER_SINGLE_USER, groupId = "${spring.kafka.consumer.groupId}", containerFactory = "nonReplying_ListenerContainerFactory")
    public void handleSingleUserFundsTransferRequest(@NotNull final ConsumerRecord<String, String> xml) {
        log.info("RECEIVED TWO BANK TRANSFER: " + xml.value());
        processor.processTransferRequestTwoBanks(xml.value());
    }
















}
