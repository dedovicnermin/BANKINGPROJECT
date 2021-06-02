package tech.nermindedovic.transformer_streams.kafka.config.processors;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.AvroBalanceMessage;
import tech.nermindedovic.library.pojos.BalanceMessage;

import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
@Slf4j
public class BalanceMessageProcessor {

    private final XmlMapper mapper;
    public BalanceMessageProcessor(final XmlMapper mapper) {
        this.mapper = mapper;
    }

    // LEG1 : Rest -> (avro->xml) -> Router
    @Bean
    public Function<KStream<String, AvroBalanceMessage>, KStream<String, String>> processBalanceLeg1() {
        return input -> input
                .mapValues(val -> {
                    try {
                        log.info(mapper.writeValueAsString(toPojo(val)));
                        return mapper.writeValueAsString(toPojo(val));
                    } catch (JsonProcessingException exception) {
                        log.error(val + "ERROR");
                        return val.toString();
                    }
                });
    }


    // LEG2: Persistence -> (xml->pojo) -> Rest
    @Bean
    public Function<KStream<String, String>, KStream<String, BalanceMessage>> processBalanceLeg2() {
        return input -> input.mapValues(val -> {
            try {
                return mapper.readValue(val, BalanceMessage.class);
            } catch (JsonProcessingException exception) {
                log.error(exception.getMessage() + "\nERROR");
                return new BalanceMessage(0,0,"0.00", true);
            }
        });
    }

    private BalanceMessage toPojo(AvroBalanceMessage balanceMessage) {
        return new BalanceMessage(balanceMessage.getAccountNumber(), balanceMessage.getRoutingNumber(), balanceMessage.getBalance(), balanceMessage.getErrors());
    }



}
