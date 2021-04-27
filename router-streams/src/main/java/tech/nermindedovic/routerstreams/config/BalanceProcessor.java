package tech.nermindedovic.routerstreams.config;

import lombok.extern.slf4j.Slf4j;
import org.jdom2.JDOMException;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
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
    public Consumer<Message<String>> balanceRequestConsumer() {
        return record -> {
            try {
                log.info("ROUTER RECIEVED:" + record);
                BalanceMessageParser parser = new BalanceMessageParser(record.getPayload());
                streamBridge.send(RouterTopicNames.OUTBOUND_BALANCE_REQUEST_PREFIX + parser.getRoute(), record);
                log.info("ROUTER SENDING :" + record);
            } catch (JDOMException | IOException | InvalidRoutingNumberException e) {
                log.error(e.getMessage());
                streamBridge.send(RouterTopicNames.OUTBOUND_BALANCE_RETURN_TOPIC, RouterAppUtils.BALANCE_ERROR_XML);
            }
        };
    }



}
