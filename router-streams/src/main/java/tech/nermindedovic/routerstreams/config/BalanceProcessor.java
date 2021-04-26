package tech.nermindedovic.routerstreams.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class BalanceProcessor {

    private final StreamBridge streamBridge;

    public BalanceProcessor(final StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    // TODO: create a balance message parser
    // TODO: implement way to direct back to transformer on invalid routing number. jdom 😵👃🏼


    @Bean
    public Consumer<String> balanceRequestConsumer() {
        return xml -> {
            String routingNumber = grabRouting(xml);
            streamBridge.send("balance.update.request." + routingNumber, xml);
        };
    }

    private String grabRouting(String xml) {
        String sTag = "<routingNumber>";
        String eTag = "</routingNumber>";
        int start = xml.lastIndexOf(sTag);
        int end = xml.indexOf(eTag);
        return xml.substring(start + "<routingNumber>".length(), end);
    }

}
