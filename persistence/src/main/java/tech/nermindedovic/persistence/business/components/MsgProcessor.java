package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
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
            return "<BalanceMessage><accountNumber>0</accountNumber><routingNumber>0</routingNumber><balance>0</balance><errors>true</errors></BalanceMessage>";
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


    private void produceErrorMessage(String errorMessage) {
        log.error("producing error message to funds.transfer.error");
        kafkaTemplate.send(PersistenceTopicNames.OUTBOUND_TRANSFER_ERRORS, errorMessage);
    }

}
