package tech.nermindedovic.routerstreams.utils;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.nermindedovic.routerstreams.config.BeanConfig;
import tech.nermindedovic.routerstreams.exception.InvalidRoutingNumberException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {BalanceMessageParser.class, BeanConfig.class})
@ExtendWith(MockitoExtension.class)
class BalanceMessageParserTest {

    @Autowired
    BalanceMessageParser balanceMessageParser;

    @Test
    void onInvalidRoute_willThrowInvalidRoutingNumberException() {
        String invalidRouteXML = "<BalanceMessage><accountNumber>123</accountNumber><routingNumber>542</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";

        assertThrows(InvalidRoutingNumberException.class, () -> balanceMessageParser.getRoute(invalidRouteXML));
    }


    @Test
    void onValidRoute_willCorrectlyOutputRoute() {
        String validRouteXML = "<BalanceMessage><accountNumber>123</accountNumber><routingNumber>111</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";
        assertDoesNotThrow(() -> balanceMessageParser.getRoute(validRouteXML));
    }

}