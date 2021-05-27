package tech.nermindedovic.transformer_streams.kafka.config.processors.avro;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import javafx.util.converter.LocalDateStringConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tech.nermindedovic.TransferMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
@Profile("avro")
@Slf4j
public class TransferMessageProcessor {
//    @Value("${spring.cloud.stream.kafka.streams.binder.configuration.schema.registry.url}")
    private String schemaRegistryUrl = "http://127.0.0.1:8081";

//    @Autowired
    private XmlMapper mapper;

//    private Serde<TransferMessage> transferMessageSerde;
    public TransferMessageProcessor(final XmlMapper mapper) {
        this.mapper = mapper;
    }

//    @Bean
//    public Serde<TransferMessage> transferMessageSerde() {
////        Map<String, String> serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
//        Map<String, String> serdeConfig = new HashMap<>();
//        serdeConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
//        serdeConfig.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
//        SpecificAvroSerializer<TransferMessage> serializer = new SpecificAvroSerializer<>();
//        SpecificAvroDeserializer<TransferMessage> deserializer = new SpecificAvroDeserializer<>();
//        Serde<TransferMessage> transferMessageSerde = Serdes.serdeFrom(serializer, deserializer);
//        transferMessageSerde.configure(serdeConfig, false);
//        return transferMessageSerde;
//    }

//    @Bean
//    public Serde<TransferMessage> transferMessageSerde() {
//        Map<String, String> serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
//        Serde<TransferMessage> transferMessageSerde = new SpecificAvroSerde<>();
//        transferMessageSerde.configure(serdeConfig, false);
//        return transferMessageSerde;
//    }

//    @PostConstruct
//    public void init() {
//        Map<String, String> serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
//        SpecificAvroSerializer<TransferMessage> setSerializer = new SpecificAvroSerializer<>();
//        SpecificAvroDeserializer<TransferMessage> setDeserializer = new SpecificAvroDeserializer<>();
//        transferMessageSerde = Serdes.serdeFrom(setSerializer, setDeserializer);
//        transferMessageSerde.configure(serdeConfig, false);
//    }


    @Bean
    public Function<KStream<String, TransferMessage>, KStream<String, String>> processTransfer() {

        return input -> input
                .mapValues(val -> {
                    log.info(val.toString());
                    try {
                        String toString = toPojoForm(val).toString();
                        log.info(mapper.writeValueAsString(toString));
                        return mapper.writeValueAsString(toString);
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
