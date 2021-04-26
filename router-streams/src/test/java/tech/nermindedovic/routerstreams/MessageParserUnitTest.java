package tech.nermindedovic.routerstreams;

import org.junit.jupiter.api.Test;
import tech.nermindedovic.routerstreams.business.domain.Account;
import tech.nermindedovic.routerstreams.business.domain.PaymentParty;


import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MessageParserUnitTest {



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
    void messageParser_willCreateParty() {
        assertThat(new MessageParser(GOOD_XML).getPaymentParty()).isEqualTo(new PaymentParty(1255543, new BigDecimal("10.00"), new Account(123, 111), new Account(345, 222)));
    }

    @Test
    void messageParser_willReturnCorrectMessageId() {
        assertThat(new MessageParser(GOOD_XML).retrieveMessageId()).isEqualTo(1255543);
    }

    @Test
    void messageParser_willReturnCorrectCount_ofValidRoutingNumbersPresent() {
        assertThat(new MessageParser(GOOD_XML).countRoutingNumbersPresent()).isEqualTo(2);
    }


    @Test
    void messageParser_willGetMatchingRoute_whenMessageContainsSameRoute_forBothParties() {
        assertThat(new MessageParser(MATCHING_ROUTE_MSG).getMatchingRoute()).isEqualTo(111L);
    }














}
