package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;

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

    public String processFailedAttempt(BalanceMessage balanceMessage) throws JsonProcessingException {
        return xmlProcessor.convertEmptyBalanceMessage(balanceMessage);
    }




}
