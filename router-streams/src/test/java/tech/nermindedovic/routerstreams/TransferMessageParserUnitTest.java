package tech.nermindedovic.routerstreams;

import org.jdom2.JDOMException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.config.BeanConfig;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;


import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {BeanConfig.class, TransferMessageParser.class})
class TransferMessageParserUnitTest {


    @Autowired TransferMessageParser transferMessageParser;


    String GOOD_XML = "<TransferMessage><messageId>1255543</messageId><debtor><accountNumber>123</accountNumber><routingNumber>111</routingNumber></debtor>" +
            "<creditor><accountNumber>345</accountNumber><routingNumber>222</routingNumber></creditor><date>12-12-2021</date>" +
            "<amount>10.00</amount><memo>My memo line</memo></TransferMessage>";

    String CONTAINING_INVALID_ROUTE = "<TransferMessage><messageId>1255543</messageId><debtor><accountNumber>123</accountNumber><routingNumber>111</routingNumber></debtor>" +
            "<creditor><accountNumber>345</accountNumber><routingNumber>567</routingNumber></creditor><date>12-12-2021</date>" +
            "<amount>10.00</amount><memo>My memo line</memo></TransferMessage>";

    String MATCHING_ROUTE_MSG = "<TransferMessage><messageId>1255543</messageId><debtor><accountNumber>123</accountNumber><routingNumber>111</routingNumber></debtor>" +
            "<creditor><accountNumber>345</accountNumber><routingNumber>111</routingNumber></creditor><date>12-12-2021</date>" +
            "<amount>10.00</amount><memo>My memo line</memo></TransferMessage>";

    @Test
    void messageParser_willCreateParty() throws JDOMException, IOException {
        assertThat(transferMessageParser.build(GOOD_XML)).isEqualTo(new PaymentData(1255543L, new BigDecimal("10.00"), new Debtor(123, 111), new Creditor(345, 222)));
    }

    @Test
    void messageParser_willReturnCorrectMessageId() throws JDOMException, IOException {
        assertThat(transferMessageParser.build(GOOD_XML).getMessageId()).isEqualTo(1255543);
    }
















}
