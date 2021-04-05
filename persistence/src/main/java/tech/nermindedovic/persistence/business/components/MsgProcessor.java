package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;



@Component
public class MsgProcessor {

    // == dependency ==
    private final XMLProcessor xmlProcessor;

    // == constructor ==
    public MsgProcessor(XMLProcessor service) {
        this.xmlProcessor = service;
    }


    /**
     * @param xml
     * @return Pass the string down to xml processor to be bind, validated, and brought back as xml
     * @throws JsonProcessingException
     */
    public String processBalanceRequest(final String xml) throws JsonProcessingException {
        return xmlProcessor.bindAndValidateBalanceRequest(xml);
    }

    /**
     * PRECONDITION: balance update request error caught
     * POSTCONDITION: returns XML of empty balanceMessage
     * @param balanceMessage
     * @return
     * @throws JsonProcessingException
     */
    public String processFailedAttempt(final BalanceMessage balanceMessage) throws JsonProcessingException {
        return xmlProcessor.convertEmptyBalanceMessage(balanceMessage);
    }


    /**
     * PRECONDITION: Received string on kafka
     * Will bind to xml and continue processing if no errors arise
     * @param xml
     * @throws JsonProcessingException
     * @throws InvalidTransferMessageException
     */
    public void processTransferRequest(final String xml) throws InvalidTransferMessageException {
        xmlProcessor.bindAndProcessTransferRequest(xml);
    }







}
