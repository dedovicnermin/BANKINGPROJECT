package tech.nermindedovic.transformer_streams.kafka.config.processors.avro;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tech.nermindedovic.library.avro.BalanceMessage;
import tech.nermindedovic.TransferMessage;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
@Profile("avro")
@Slf4j
public class BalanceMessageProcessor {

//    @Value("${spring.cloud.stream.kafka.streams.binder.configuration.schema.registry.url}")
    private String schemaRegistryUrl = "http://127.0.0.1:8081";

    @Autowired
    private XmlMapper mapper;

    private Serde<BalanceMessage> balanceMessageSerde;



//    public BalanceMessageProcessor(final XmlMapper mapper) {
//        this.mapper = mapper;
//    }

//    @Bean
//    public Serde<BalanceMessage> balanceMessageSerde() {
//        Map<String, String> serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
//        SpecificAvroSerializer<BalanceMessage> serializer = new SpecificAvroSerializer<>();
//        SpecificAvroDeserializer<BalanceMessage> deserializer = new SpecificAvroDeserializer<>();
//        Serde<BalanceMessage> balanceMessageSerde = Serdes.serdeFrom(serializer, deserializer);
//        balanceMessageSerde.configure(serdeConfig, false);
//        return balanceMessageSerde;
//    }

//    @PostConstruct
//    public void init() {
//        Map<String, String> serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
//        SpecificAvroSerializer<BalanceMessage> setSerializer = new SpecificAvroSerializer<>();
//        SpecificAvroDeserializer<BalanceMessage> setDeserializer = new SpecificAvroDeserializer<>();
//        balanceMessageSerde = Serdes.serdeFrom(setSerializer, setDeserializer);
//        balanceMessageSerde.configure(serdeConfig, false);
//
//    }


    @Bean
    public Function<KStream<String, BalanceMessage>, KStream<String, String>> processBalanceLeg1() {
        return input -> input
                .mapValues(val -> {
                    try {
                        log.info("\n" + val + "\n");
                        log.info(mapper.writeValueAsString(val));
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
                log.info(mapper.readValue(val, BalanceMessage.class).toString());
                return mapper.readValue(val, BalanceMessage.class);
            } catch (JsonProcessingException exception) {
                return null;
            }
        });
    }



}
