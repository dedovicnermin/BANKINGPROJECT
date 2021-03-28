package tech.nermindedovic.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.nermindedovic.transformer.components.MessageTransformer;
import tech.nermindedovic.transformer.pojos.BalanceMessage;
import tech.nermindedovic.transformer.pojos.Creditor;
import tech.nermindedovic.transformer.pojos.Debtor;
import tech.nermindedovic.transformer.pojos.TransferMessage;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TransformerComponentTest {

    @Autowired
    private MessageTransformer messageTransformer;

    @Test
    void test_onValidBalanceMessage_shouldNotThrowOn_XML_Transformation() throws JsonProcessingException {
        BalanceMessage balanceMessage = createBalanceMessage(1,159595, "0.00", false);

        String xml = messageTransformer.balancePojoToXML(balanceMessage);

        Assertions.assertDoesNotThrow(() -> JsonProcessingException.class);
        assertThat(mapper.writeValueAsString(balanceMessage)).isEqualTo(xml);

    }




    @Test
    void test_onValidXMLResponse_shouldNotThrow_on_xmlToPojo() throws JsonProcessingException {
        BalanceMessage balanceMessage = createBalanceMessage(1, 34234, "200.25", false);
        String xml = mapper.writeValueAsString(balanceMessage);

        assertThat(messageTransformer.balanceXMLToPojo(xml)).isEqualTo(balanceMessage);
        Assertions.assertDoesNotThrow(() -> JsonProcessingException.class);

    }






    private TransferMessage createTransferMessage(long message_id, Creditor creditor, Debtor debtor, double amount, String memo) {
        return new TransferMessage(message_id, creditor, debtor, new Date(), new BigDecimal(String.valueOf(amount)),  memo);
    }

    private BalanceMessage createBalanceMessage(long accountNumber, long routingNumber, String balance, boolean errors) {
        return new BalanceMessage(accountNumber, routingNumber, balance, errors);
    }

    private XmlMapper mapper = new XmlMapper();

}
