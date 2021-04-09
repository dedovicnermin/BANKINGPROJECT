package tech.nermindedovic.transformer.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import tech.nermindedovic.transformer.pojos.BalanceMessage;
import tech.nermindedovic.transformer.pojos.TransferMessage;



@Component
public class MessageTransformer {


    // == dependencies ==
    // == ? would there be an advantage to making object into bean & autowiring?
    private final XmlMapper xmlMapper = new XmlMapper();



    public String balancePojoToXML(final BalanceMessage balanceMessage) throws JsonProcessingException {
        return xmlMapper.writeValueAsString(balanceMessage);
    }

    public BalanceMessage balanceXMLToPojo(final String xml) throws JsonProcessingException {
        return xmlMapper.readValue(xml, BalanceMessage.class);
    }



    public String transferPojoToXML(final TransferMessage transferMessage) throws JsonProcessingException {
        return xmlMapper.writeValueAsString(transferMessage);
    }

}
