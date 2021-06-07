package tech.nermindedovic.routerstreams.config.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.routerstreams.utils.BalanceMessageParser;



import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
@Slf4j
public class BalanceProcessor {

    //  TESTED : 👍🏼

    public final Predicate<String, String> isRoute111 = (key,val) -> key.equals("111");
    public final Predicate<String, String> isRoute222 = (key,val) -> key.equals("222");
    public final Predicate<String, String> isUnknownRoute = (key, val) -> true;

    private final BalanceMessageParser parser;
    public BalanceProcessor(final BalanceMessageParser balanceMessageParser) {
        this.parser = balanceMessageParser;
    }


    /**
     * IN: balance.update.request
     * OUT:
     *    [0] - balance.update.request.111
     *    [1] - balance.update.request.222
     *    [2] - balance.update.response
     *
     * @return KStream[]
     */
    @Bean
    @SuppressWarnings("unchecked")
    public Function<KStream<String, String>, KStream<String, String>[]> balanceRequestProcessor() {
         return input -> input
                .selectKey((key, value) -> parser.getRoute(value))
                .branch(isRoute111, isRoute222, isUnknownRoute);
    }



}
