package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.nermindedovic.persistence.business.doman.*;
import tech.nermindedovic.persistence.business.service.PersistenceService;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;


@Component
@Slf4j
public class MsgProcessor {


    // == dependency ==
    private final PersistenceService persistenceService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${persistence-topics.OUTBOUND_TRANSFER_ERRORS}")
    private String errorTopic;

    @Value("${persistence-topics.OUTBOUND_TRANSFER_STATUS}")
    private String transferStatusTopic;


    @Value("${routing-number}")
    private long routingNumber;

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
     * Called when routing numbers in transfer message match
     * bind string to TransferMessage. attempt to persist transaction. else produce error message
     * @param xml of type TransferMessage
     *
     */
    public void processTransferRequest(final String key, final String xml)  {
        try {
            TransferMessage transferMessage = BankXmlBinder.toTransferMessage(xml);
            persistenceService.validateAndProcessTransferMessage(transferMessage);
            updateState(key, TransferStatus.PERSISTED);
        } catch (JsonProcessingException | InvalidTransferMessageException e) {
            produceErrorMessage("PERSISTENCE --- "+e.getMessage());
            updateState(key, TransferStatus.FAIL);
        }
    }




    /**
     * bind string to TransferMessage. attempt to persist transaction. else produce error message
     * @param xml of type TransferMessage
     *
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


    /**
     * Get the account belonging to this bank (routing number 111 || 222)
     * @param transferMessage holding account info
     * @return account number
     */
    private long retrieveNativeAccount(TransferMessage transferMessage) {
        Creditor creditor = transferMessage.getCreditor();
        Debtor debtor = transferMessage.getDebtor();
        if (creditor.getRoutingNumber() == routingNumber) return creditor.getAccountNumber();
        else return debtor.getAccountNumber();
    }


    /**
     * validating native account. To be sent to Router app.
     * @param key messageId
     * @param json transferValidation
     * @return return transferValidation:JSON
     */
    public String processTransferValidation(String key, String json) {
        try {
            TransferValidation validation = BankXmlBinder.toTransferValidation(json);
            persistenceService.processTransferValidation(validation);
            return BankXmlBinder.toJson(validation);
        } catch (JsonProcessingException e) {
            updateState(key, TransferStatus.FAIL);
            return json;
        }
    }


    /**
     * Updates state store found in router app
     * @param messageId transfer ID
     * @param status status of transfer
     */
    private void updateState(String messageId, TransferStatus status) {
        kafkaTemplate.send(new ProducerRecord<>(transferStatusTopic, messageId, status.name()));
    }

    private boolean isDebtor(TransferMessage transferMessage, long accountNumber) {
        return transferMessage.getDebtor().getAccountNumber() == accountNumber;
    }


    private void produceErrorMessage(String errorMessage) {
        log.error("producing error message to funds.transfer.error");
        kafkaTemplate.send(errorTopic, errorMessage);
    }



}
