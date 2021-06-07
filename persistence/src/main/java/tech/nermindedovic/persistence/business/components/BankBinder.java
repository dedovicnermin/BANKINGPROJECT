package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.library.pojos.TransferMessage;
import tech.nermindedovic.library.pojos.TransferStatus;



@Component
public class BankBinder {

    private final XmlMapper xmlMapper;
    private final ObjectMapper jsonMapper;

    public BankBinder(final XmlMapper xmlMapper, final ObjectMapper objectMapper) {
        this.xmlMapper = xmlMapper;
        this.jsonMapper = objectMapper;
    }


    public BalanceMessage toBalanceMessage(final String xml) throws JsonProcessingException {
        return xmlMapper.readValue(xml, BalanceMessage.class);
    }


    public String toXml(final BalanceMessage balanceMessage) throws JsonProcessingException {
        return xmlMapper.writeValueAsString(balanceMessage);
    }

    public TransferMessage toTransferMessage(final String xml) throws JsonProcessingException {
        return xmlMapper.readValue(xml, TransferMessage.class);
    }


    public String toJson(final TransferStatus status) {
        try {
            return jsonMapper.writeValueAsString(status);
        } catch (JsonProcessingException e) {
            return String.format("{\n" + "   \"TransferStatus\": \"%s\"" + "\n}", status);
        }
    }











}
