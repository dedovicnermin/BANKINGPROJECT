package tech.nermindedovic.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.nermindedovic.transformer.components.MessageTransformer;
import tech.nermindedovic.transformer.business.pojos.BalanceMessage;
import tech.nermindedovic.transformer.business.pojos.Creditor;
import tech.nermindedovic.transformer.business.pojos.Debtor;
import tech.nermindedovic.transformer.business.pojos.TransferMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class TransformerComponentTest {


    private final MessageTransformer messageTransformer = new MessageTransformer();

    private final XmlMapper mapper = new XmlMapper();

    /**
     * Testing balance message request transformation
     */
    @Test
    void test_onValidBalanceMessage_shouldNotThrowOn_XML_Transformation() throws JsonProcessingException {
        BalanceMessage balanceMessage = createBalanceMessage(1,159595, "0.00");

        String xml = messageTransformer.balancePojoToXML(balanceMessage);
        assertThat(mapper.writeValueAsString(balanceMessage)).isEqualTo(xml);

    }



    /**
     * Testing balance message response from rest
     */
    @Test
    void test_onValidXMLResponse_shouldNotThrow_on_xmlToPojo() throws JsonProcessingException {
        BalanceMessage balanceMessage = createBalanceMessage(1, 34234, "200.25");
        String xml = mapper.writeValueAsString(balanceMessage);

        assertThat(messageTransformer.balanceXMLToPojo(xml)).isEqualTo(balanceMessage);
    }



    @Test
    void test_onValidTransferMessage_transformsToValidXML() throws JsonProcessingException {


        LocalDate date = LocalDate.now();
        String xml = "<TransferMessage><messageId>1234</messageId><creditor><accountNumber>21345</accountNumber><routingNumber>3454</routingNumber></creditor><debtor><accountNumber>123455</accountNumber><routingNumber>45555</routingNumber></debtor><date>" + date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")) + "</date><amount>10</amount><memo>memo string</memo></TransferMessage>";
        assertThat(messageTransformer.transferPojoToXML(new TransferMessage(1234, new Creditor(21345,3454), new Debtor(123455, 45555), date, BigDecimal.TEN, "memo string"))).isEqualTo(xml);
    }


    @Test
    void test_onInvalidTransferMessage_returnsNull() throws JsonProcessingException {
        assertThat(messageTransformer.transferPojoToXML(null)).isEqualTo("<null/>");
    }





    private BalanceMessage createBalanceMessage(long accountNumber, long routingNumber, String balance) {
        return new BalanceMessage(accountNumber, routingNumber, balance, false);
    }



}
