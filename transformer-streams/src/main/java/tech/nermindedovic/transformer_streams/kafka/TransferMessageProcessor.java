package tech.nermindedovic.transformer_streams.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.transformer_streams.pojos.TransferMessage;


import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
public class TransferMessageProcessor {


    XmlMapper mapper = new XmlMapper();

    @Bean
    public Function<KStream<String, TransferMessage>, KStream<String, String>> processTransfer() {

        return input -> input
                .mapValues(val -> {
                    try {
                        return mapper.writeValueAsString(val);
                    } catch (JsonProcessingException exception) {
                        exception.printStackTrace();
                        return null;
                    }
                });
    }






}
