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



    public BalanceMessage processBalanceRequest(String xml) throws JsonProcessingException {
        BalanceMessage balanceMessage = xmlProcessor.bindAndValidate(xml);
        return balanceMessage;
    }


}
