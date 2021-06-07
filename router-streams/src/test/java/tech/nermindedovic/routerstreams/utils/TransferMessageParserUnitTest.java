package tech.nermindedovic.routerstreams.utils;

import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Test;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;


import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


class TransferMessageParserUnitTest {


    final SAXBuilder builder = new SAXBuilder();
    final TransferMessageParser parser = new TransferMessageParser(builder);


    String GOOD_XML = "<TransferMessage><messageId>1255543</messageId><debtor><accountNumber>123</accountNumber><routingNumber>111</routingNumber></debtor>" +
            "<creditor><accountNumber>345</accountNumber><routingNumber>222</routingNumber></creditor><date>12-12-2021</date>" +
            "<amount>10.00</amount><memo>My memo line</memo></TransferMessage>";



    @Test
    void messageParser_willCreateParty() {
        PaymentData data = PaymentData.builder()
                .messageId(1255543L)
                .debtorAccount(new Debtor(123, 111))
                .creditorAccount(new Creditor(345, 222))
                .amount(BigDecimal.TEN.setScale(2 ,BigDecimal.ROUND_HALF_UP))
                .transferMessageXml(GOOD_XML)
                .build();

        assertThat(parser.build(GOOD_XML)).isEqualTo(data);
    }


    @Test
    void onInvalidXML_willReturnPaymentData_withXMLStored() {
        PaymentData data = new PaymentData();
        String xml = "<BAD_XML>";
        data.setTransferMessageXml(xml);

        assertThat(parser.build(xml)).isEqualTo(data);
    }
















}
