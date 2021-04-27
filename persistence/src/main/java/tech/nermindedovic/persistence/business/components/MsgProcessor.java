package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.nermindedovic.persistence.business.doman.*;
import tech.nermindedovic.persistence.business.service.PersistenceService;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;
import tech.nermindedovic.persistence.kafka.PersistenceTopicNames;



@Component
@Slf4j
public class MsgProcessor {


    // == dependency ==
    private final PersistenceService persistenceService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // == constructor ==
    public MsgProcessor(PersistenceService persistenceService, KafkaTemplate<String,String> kafkaTemplate) {
        this.persistenceService = persistenceService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * bind string to pojo. Enrich pojo. bind to string and return.
     * @param xml of type BalanceMessage
     * @return xml of BalanceMessage response
     */
    public String processBalanceRequest(final String xml)  {
        try {
            BalanceMessage balanceMessage = BankXmlBinder.toBalanceMessage(xml);
            persistenceService.validateBalanceMessage(balanceMessage);
            return BankXmlBinder.toXml(balanceMessage);
        } catch (JsonProcessingException processingException) {
            return "<BalanceMessage><accountNumber>0</accountNumber><routingNumber>0</routingNumber><balance></balance><errors>true</errors></BalanceMessage>";
        }
    }


    /**
     * bind string to TransferMessage. attempt to persist transaction. else produce error message
     * @param xml of type TransferMessage
     */
    public void processTransferRequest(final String xml)  {
        try {
            TransferMessage transferMessage = BankXmlBinder.toTransferMessage(xml);
            persistenceService.validateAndProcessTransferMessage(transferMessage);
        } catch (JsonProcessingException e) {
            produceErrorMessage("PERSISTENCE --- Unable to bind XML to TransferMessagePOJO");
        } catch (InvalidTransferMessageException e) {
            produceErrorMessage("PERSISTENCE --- "+e.getMessage());
        }
    }


    /**
     * bind string to TransferMessage. attempt to persist transaction. else produce error message
     * @param xml of type TransferMessage
     */
    public void processTransferRequestTwoBanks(final String xml)  {
        try {
            TransferMessage transferMessage = BankXmlBinder.toTransferMessage(xml);
            long accountNumberReference = retrieveNativeAccount(transferMessage);
            boolean isDebtor = isDebtor(transferMessage, accountNumberReference);
            persistenceService.processTwoBankTransferMessage(transferMessage, accountNumberReference, isDebtor);
        } catch (JsonProcessingException e) {
            produceErrorMessage("PERSISTENCE --- Unable to bind XML to TransferMessagePOJO");
        } catch (InvalidTransferMessageException e) {
            produceErrorMessage("PERSISTENCE --- TransferMessage already exists");
        }
    }

    private long retrieveNativeAccount(TransferMessage transferMessage) {
        Creditor creditor = transferMessage.getCreditor();
        Debtor debtor = transferMessage.getDebtor();
        if (creditor.getRoutingNumber() == 111L) return creditor.getAccountNumber();
        else return debtor.getAccountNumber();
    }

    private boolean isDebtor(TransferMessage transferMessage, long accountNumber) {
        return transferMessage.getDebtor().getAccountNumber() == accountNumber;
    }



    // TODO : unbindable exception should ensure the currentLeg gets set to 0
    public String processTransferValidation(final String json) {
        try {
            TransferValidation validation = BankXmlBinder.toTransferValidation(json);
            persistenceService.processTransferValidation(validation);
            return BankXmlBinder.toJson(validation);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return json;
        }
    }


    private void produceErrorMessage(String errorMessage) {
        log.error("producing error message to funds.transfer.error");
        kafkaTemplate.send(PersistenceTopicNames.OUTBOUND_TRANSFER_ERRORS, errorMessage);
    }



}
