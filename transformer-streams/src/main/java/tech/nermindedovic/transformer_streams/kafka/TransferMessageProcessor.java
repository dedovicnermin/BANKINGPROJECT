package tech.nermindedovic.transformer_streams.kafka;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.library.pojos.TransferMessage;



import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
public class TransferMessageProcessor {


    private final XmlMapper mapper;
    public TransferMessageProcessor(final XmlMapper mapper) {
        this.mapper = mapper;
    }

    @Bean
    public Function<KStream<String, TransferMessage>, KStream<String, String>> processTransfer() {

        return input -> input
                .mapValues(val -> {
                    try {
                        return mapper.writeValueAsString(val);
                    } catch (JsonProcessingException exception) {
                        exception.printStackTrace();
                        return val.toString();
                    }
                });
    }






}
