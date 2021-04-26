package tech.nermindedovic.transformer_streams.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.transformer_streams.pojos.BalanceMessage;

import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
@Slf4j
public class BalanceMessageProcessor {


    private final XmlMapper mapper = new XmlMapper();

    @Bean
    public Function<KStream<String, BalanceMessage>, KStream<String, String>> processBalanceLeg1() {
        return input -> input
                .mapValues(val -> {
                    try {
                        log.info("INPUT from rest:" + val);
                        return mapper.writeValueAsString(val);
                    } catch (JsonProcessingException exception) {
                        exception.printStackTrace();
                        return null;
                    }
                });
    }


    @Bean
    public Function<KStream<String, String>, KStream<String, BalanceMessage>> processBalanceLeg2() {
        return input -> input.mapValues(val -> {
            try {
                log.info("INPUT from persistence " + val);
                return mapper.readValue(val, BalanceMessage.class);
            } catch (JsonProcessingException exception) {
                exception.printStackTrace();
                return null;
            }
        });
    }



}
