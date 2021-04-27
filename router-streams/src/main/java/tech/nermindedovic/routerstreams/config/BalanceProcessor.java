package tech.nermindedovic.routerstreams.config;

import lombok.extern.slf4j.Slf4j;
import org.jdom2.JDOMException;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.routerstreams.exception.InvalidRoutingNumberException;
import tech.nermindedovic.routerstreams.utils.BalanceMessageParser;
import tech.nermindedovic.routerstreams.utils.RouterAppUtils;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

import java.io.IOException;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class BalanceProcessor {

    private final StreamBridge streamBridge;

    public BalanceProcessor(final StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }


    @Bean
    public Consumer<String> balanceRequestConsumer() {
        return xml -> {
            try {
                log.info("ROUTER RECIEVED:" + xml);
                BalanceMessageParser parser = new BalanceMessageParser(xml);
                streamBridge.send(RouterTopicNames.OUTBOUND_BALANCE_REQUEST_PREFIX + parser.getRoute(), xml);
                log.info("ROUTER SENDING :" + xml);
            } catch (JDOMException | IOException | InvalidRoutingNumberException e) {
                log.error(e.getMessage());
                streamBridge.send(RouterTopicNames.OUTBOUND_BALANCE_RETURN_TOPIC, RouterAppUtils.BALANCE_ERROR_XML);
            }
        };
    }



}
