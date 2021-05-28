package tech.nermindedovic.transformer_streams.kafka.config.processors.avro;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tech.nermindedovic.TransferMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
@Profile("avro")
@Slf4j
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
                        return mapper.writeValueAsString(toPojoForm(val));
                    } catch (JsonProcessingException exception) {
                        exception.printStackTrace();
                        return val.toString();
                    }
                });
    }

    private tech.nermindedovic.library.pojos.TransferMessage toPojoForm(TransferMessage transferMessage) {
        return tech.nermindedovic.library.pojos.TransferMessage.builder()
                .messageId(transferMessage.getMessageId())
                .creditor(new Creditor(transferMessage.getCreditor().getAccountNumber(), transferMessage.getCreditor().getRoutingNumber()))
                .debtor(new Debtor(transferMessage.getDebtor().getAccountNumber(), transferMessage.getDebtor().getRoutingNumber()))
                .date(LocalDate.parse(transferMessage.getDate()))
                .amount(new BigDecimal(transferMessage.getAmount()))
                .memo(transferMessage.getMemo())
                .build();
    }






}
