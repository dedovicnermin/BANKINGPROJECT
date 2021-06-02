package tech.nermindedovic.transformer_streams.kafka.config.processors;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.AvroTransferMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
@Slf4j
public class TransferMessageProcessor {

    private final XmlMapper mapper;
    public TransferMessageProcessor(final XmlMapper mapper) {
        this.mapper = mapper;
    }


    @Bean
    public Function<KStream<String, AvroTransferMessage>, KStream<String, String>> processTransfer() {

        return input -> input
                .mapValues(val -> {
                    try {
                        log.info(mapper.writeValueAsString(toPojoForm(val)));
                        return mapper.writeValueAsString(toPojoForm(val));
                    } catch (JsonProcessingException exception) {
                        exception.printStackTrace();
                        return val + " - error!";
                    }
                });
    }

    private TransferMessage toPojoForm(AvroTransferMessage transferMessage) {
        return TransferMessage.builder()
                .messageId(transferMessage.getMessageId())
                .creditor(new Creditor(transferMessage.getCreditor().getAccountNumber(), transferMessage.getCreditor().getRoutingNumber()))
                .debtor(new Debtor(transferMessage.getDebtor().getAccountNumber(), transferMessage.getDebtor().getRoutingNumber()))
                .date(LocalDate.parse(transferMessage.getDate()))
                .amount(new BigDecimal(transferMessage.getAmount()))
                .memo(transferMessage.getMemo())
                .build();
    }









}
