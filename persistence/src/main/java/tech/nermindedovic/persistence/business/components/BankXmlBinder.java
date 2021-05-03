package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.library.pojos.TransferMessage;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.library.pojos.TransferValidation;

public class BankXmlBinder {

    private BankXmlBinder() {}

    private static final XmlMapper xmlMapper = new XmlMapper();
    private static final ObjectMapper jsonMapper = new ObjectMapper();


    public static BalanceMessage toBalanceMessage(final String xml) throws JsonProcessingException {
        return xmlMapper.readValue(xml, BalanceMessage.class);
    }


    public static String toXml(final BalanceMessage balanceMessage) throws JsonProcessingException {
        return xmlMapper.writeValueAsString(balanceMessage);
    }

    public static TransferMessage toTransferMessage(final String xml) throws JsonProcessingException {
        return xmlMapper.readValue(xml, TransferMessage.class);
    }


    public static TransferValidation toTransferValidation(final String json) throws JsonProcessingException {
        return jsonMapper.readValue(json, TransferValidation.class);
    }


    public static String toJson(final TransferValidation transferValidation) throws JsonProcessingException {
        return jsonMapper.writeValueAsString(transferValidation);
    }


    public static String toJson(final TransferStatus status) {
        try {
            return jsonMapper.writeValueAsString(status);
        } catch (JsonProcessingException e) {
            return String.format("{\n" + "   \"TransferStatus\": \"%s\"" + "\n}", status);
        }
    }











}
