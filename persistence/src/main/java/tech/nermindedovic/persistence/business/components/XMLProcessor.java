package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sun.istack.NotNull;
import org.springframework.stereotype.Component;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.service.PersistenceService;

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



    public String convertToXml(final BalanceMessage balanceMessage) throws JsonProcessingException {
        return xMapper.writeValueAsString(balanceMessage);
    }



    public String convertEmptyBalanceMessage(BalanceMessage balanceMessage) throws JsonProcessingException {
        return xMapper.writeValueAsString(balanceMessage);
    }








}
