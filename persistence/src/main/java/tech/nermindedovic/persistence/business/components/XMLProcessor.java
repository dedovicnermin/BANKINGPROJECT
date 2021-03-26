package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.business.service.PersistenceService;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

@Component
public class XMLProcessor {

    // == fields ==
    private final XmlMapper xMapper = new XmlMapper();

    // == dependency ==
    private final PersistenceService persistenceService;

    // == constructor ==
    public XMLProcessor(PersistenceService service) {
        this.persistenceService = service;
    }






    /**
     * @param xml string passed from the balance request topic sub
     * @return balance message or throw error for invalid xml string passed via kafka
     */
    public String bindAndValidateBalanceRequest(final String xml) throws JsonProcessingException {
        BalanceMessage balanceMessage = xMapper.readValue(xml, BalanceMessage.class);
        persistenceService.validateBalanceMessage(balanceMessage);
        return convertToXml(balanceMessage);
    }


    /**
     * PRECONDITION: balanceRequest has been processed without errors. Heading back outbound to consumer service to reply
     * POSTCONDITION: balanceRequest transformed into xml
     * @param balanceMessage
     * @return
     * @throws JsonProcessingException
     */
    public String convertToXml(final BalanceMessage balanceMessage) throws JsonProcessingException {
        return xMapper.writeValueAsString(balanceMessage);
    }


    /**
     * PRECONDITION: error in balanceMessage. New balanceMessage created/populated with errors set to true
     * POSTCONDITION: generic balanceMessage converted to XML to be sent back to kafka
     * @param balanceMessage
     * @return
     * @throws JsonProcessingException
     */
    public String convertEmptyBalanceMessage(final BalanceMessage balanceMessage) throws JsonProcessingException {
        return xMapper.writeValueAsString(balanceMessage);
    }


    /**
     *
     * @param xml
     * @throws JsonProcessingException
     * @throws InvalidTransferMessageException
     */
    public void bindAndProcessTransferRequest(final String xml) throws JsonProcessingException, InvalidTransferMessageException {
        TransferMessage transferMessage = xMapper.readValue(xml, TransferMessage.class);
        persistenceService.validateAndProcessTransferMessage(transferMessage);
    }








}
