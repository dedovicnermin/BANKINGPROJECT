package tech.nermindedovic.routerstreams.utils;


import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class BalanceMessageParserTest {

    SAXBuilder builder = new SAXBuilder();
    BalanceMessageParser balanceMessageParser = new BalanceMessageParser(builder);

    final static String ERROR_RETURN = "0";

    @Test
    void onInvalidRoute_willReturn0() {
        String invalidRouteXML = "<BalanceMessage><accountNumber>123</accountNumber><routingNumber>542</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";
        assertThat(balanceMessageParser.getRoute(invalidRouteXML)).isEqualTo(ERROR_RETURN);
    }


    @Test
    void onInvalidXML_willReturn0() {
        String invalidXML = "<XML_BAD>";
        assertThat(balanceMessageParser.getRoute(invalidXML)).isEqualTo(ERROR_RETURN);
    }


    @Test
    void onValidXML_withValidRoute_returnsCorrectRouting() {
        String balanceMsgXML = "<BalanceMessage><accountNumber>123</accountNumber><routingNumber>111</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";
        String balanceMsgXML2 = "<BalanceMessage><accountNumber>123</accountNumber><routingNumber>222</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";
        assertThat(balanceMessageParser.getRoute(balanceMsgXML)).isEqualTo("111");
        assertThat(balanceMessageParser.getRoute(balanceMsgXML2)).isEqualTo("222");
    }







}