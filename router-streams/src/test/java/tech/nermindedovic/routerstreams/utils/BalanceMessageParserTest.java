package tech.nermindedovic.routerstreams.utils;

import org.junit.jupiter.api.Test;
import tech.nermindedovic.routerstreams.exception.InvalidRoutingNumberException;

import static org.junit.jupiter.api.Assertions.*;

class BalanceMessageParserTest {


    @Test
    void onInvalidRoute_willThrowInvalidRoutingNumberException() {
        String invalidRouteXML = "<BalanceMessage><accountNumber>123</accountNumber><routingNumber>542</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";
        assertThrows(InvalidRoutingNumberException.class, () -> new BalanceMessageParser(invalidRouteXML));
    }


    @Test
    void onValidRoute_willCorrectlyOutputRoute() {
        String validRouteXML = "<BalanceMessage><accountNumber>123</accountNumber><routingNumber>111</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";
        assertDoesNotThrow(() -> new BalanceMessageParser(validRouteXML));
    }

}