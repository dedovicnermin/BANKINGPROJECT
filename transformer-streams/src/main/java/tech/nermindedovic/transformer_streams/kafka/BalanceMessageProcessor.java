package tech.nermindedovic.transformer_streams.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.library.pojos.BalanceMessage;


import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
public class BalanceMessageProcessor {


    private final XmlMapper mapper;
    public BalanceMessageProcessor(final XmlMapper mapper) {
        this.mapper = mapper;
    }

    @Bean
    public Function<KStream<String, BalanceMessage>, KStream<String, String>> processBalanceLeg1() {
        return input -> input
                .mapValues(val -> {
                    try {
                        return mapper.writeValueAsString(val);
                    } catch (JsonProcessingException exception) {
                        return val.toString();
                    }
                });
    }


    @Bean
    public Function<KStream<String, String>, KStream<String, BalanceMessage>> processBalanceLeg2() {
        return input -> input.mapValues(val -> {
            try {
                return mapper.readValue(val, BalanceMessage.class);
            } catch (JsonProcessingException exception) {
                return null;
            }
        });
    }



}
