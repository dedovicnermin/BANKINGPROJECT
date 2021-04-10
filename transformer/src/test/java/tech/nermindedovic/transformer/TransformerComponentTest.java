package tech.nermindedovic.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.nermindedovic.transformer.components.MessageTransformer;
import tech.nermindedovic.transformer.pojos.BalanceMessage;
import tech.nermindedovic.transformer.pojos.Creditor;
import tech.nermindedovic.transformer.pojos.Debtor;
import tech.nermindedovic.transformer.pojos.TransferMessage;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TransformerComponentTest {

    @Autowired
    private MessageTransformer messageTransformer;

    private XmlMapper mapper = new XmlMapper();

    /**
     * Testing balance message request transformation
     * @throws JsonProcessingException
     */
    @Test
    void test_onValidBalanceMessage_shouldNotThrowOn_XML_Transformation() throws JsonProcessingException {
        BalanceMessage balanceMessage = createBalanceMessage(1,159595, "0.00", false);

        String xml = messageTransformer.balancePojoToXML(balanceMessage);

        Assertions.assertDoesNotThrow(() -> JsonProcessingException.class);
        assertThat(mapper.writeValueAsString(balanceMessage)).isEqualTo(xml);

    }



    /**
     * Testing balance message response from rest
     * @throws JsonProcessingException
     */
    @Test
    void test_onValidXMLResponse_shouldNotThrow_on_xmlToPojo() throws JsonProcessingException {
        BalanceMessage balanceMessage = createBalanceMessage(1, 34234, "200.25", false);
        String xml = mapper.writeValueAsString(balanceMessage);

        assertThat(messageTransformer.balanceXMLToPojo(xml)).isEqualTo(balanceMessage);
        Assertions.assertDoesNotThrow(() -> JsonProcessingException.class);
    }



    @Test
    void test_onValidTransferMessage_transformsToValidXML() throws JsonProcessingException {

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        ZoneId zoneId = ZoneId.of("America/Chicago");
        dateFormat.setTimeZone(TimeZone.getTimeZone(zoneId));
        String dateString = dateFormat.format(date);

        String xml = "<TransferMessage><messageId>1234</messageId><creditor><accountNumber>21345</accountNumber><routingNumber>3454</routingNumber></creditor><debtor><accountNumber>123455</accountNumber><routingNumber>45555</routingNumber></debtor><date>" + dateString + "</date><amount>10</amount><memo>memo string</memo></TransferMessage>";
        assertThat(messageTransformer.transferPojoToXML(new TransferMessage(1234, new Creditor(21345,3454), new Debtor(123455, 45555), date, BigDecimal.TEN, "memo string"))).isEqualTo(xml);
    }


    @Test
    void test_onInvalidTransferMessage_returnsNull() throws JsonProcessingException {
        assertThat(messageTransformer.transferPojoToXML(null)).isEqualTo("<null/>");
    }







    private BalanceMessage createBalanceMessage(long accountNumber, long routingNumber, String balance, boolean errors) {
        return new BalanceMessage(accountNumber, routingNumber, balance, errors);
    }



}
