package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.service.PersistenceService;

@Component
public class XMLProcessor {

    // == fields ==
    private final XmlMapper xMapper = new XmlMapper();

    // == dependency ==
    private final PersistenceService dbService;

    // == constructor ==
    public XMLProcessor(PersistenceService service) {
        this.dbService = service;
    }


    /**
     * @param xml string passed from the balance request topic sub
     * @return balance message or throw error for invalid xml string passed via kafka
     */
    public BalanceMessage bindAndValidate(String xml) throws JsonProcessingException {
        BalanceMessage balanceMessage = xMapper.readValue(xml, BalanceMessage.class);
        dbService.validateBalanceMessage(balanceMessage);
        return balanceMessage;
    }





}
