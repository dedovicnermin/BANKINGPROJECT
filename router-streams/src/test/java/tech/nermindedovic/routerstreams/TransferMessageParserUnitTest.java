package tech.nermindedovic.routerstreams;

import org.jdom2.JDOMException;
import org.junit.jupiter.api.Test;
import tech.nermindedovic.routerstreams.business.domain.Account;
import tech.nermindedovic.routerstreams.business.domain.PaymentParty;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;


import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransferMessageParserUnitTest {



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
        assertThat(new TransferMessageParser(GOOD_XML).getPaymentParty()).isEqualTo(new PaymentParty(1255543, new BigDecimal("10.00"), new Account(123, 111), new Account(345, 222)));
    }

    @Test
    void messageParser_willReturnCorrectMessageId() throws JDOMException, IOException {
        assertThat(new TransferMessageParser(GOOD_XML).retrieveMessageId()).isEqualTo(1255543);
    }

    @Test
    void messageParser_willReturnCorrectCount_ofValidRoutingNumbersPresent() throws JDOMException, IOException {
        assertThat(new TransferMessageParser(GOOD_XML).countRoutingNumbersPresent()).isEqualTo(2);
    }


    @Test
    void messageParser_willGetMatchingRoute_whenMessageContainsSameRoute_forBothParties() throws JDOMException, IOException {
        assertThat(new TransferMessageParser(MATCHING_ROUTE_MSG).getMatchingRoute()).isEqualTo(111L);
    }














}
