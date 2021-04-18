package tech.nermindedovic.persistence.business.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.doman.TransferMessage;

public final class BankXmlBinder {

    private static final XmlMapper mapper = new XmlMapper();

    public static BalanceMessage toBalanceMessage(final String xml) throws JsonProcessingException {
        return mapper.readValue(xml, BalanceMessage.class);
    }


    public static String toXml(final BalanceMessage balanceMessage) throws JsonProcessingException {
        return mapper.writeValueAsString(balanceMessage);
    }

    public static TransferMessage toTransferMessage(final String xml) throws JsonProcessingException {
        return mapper.readValue(xml, TransferMessage.class);
    }







}
