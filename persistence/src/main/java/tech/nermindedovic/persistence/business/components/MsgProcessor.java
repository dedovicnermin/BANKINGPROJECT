package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import java.util.Optional;

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
    public String processBalanceRequest(String xml) throws JsonProcessingException {
        String balanceMessage = xmlProcessor.bindAndValidateBalanceRequest(xml);
        return balanceMessage;
    }

    /**
     * PRECONDITION: balance update request error caught
     * POSTCONDITION: returns XML of empty balanceMessage
     * @param balanceMessage
     * @return
     * @throws JsonProcessingException
     */
    public String processFailedAttempt(BalanceMessage balanceMessage) throws JsonProcessingException {
        return xmlProcessor.convertEmptyBalanceMessage(balanceMessage);
    }


    public void processTransferRequest(String xml) throws JsonProcessingException, InvalidTransferMessageException {
        xmlProcessor.bindAndProcessTransferRequest(xml);
        return;
    }







}
