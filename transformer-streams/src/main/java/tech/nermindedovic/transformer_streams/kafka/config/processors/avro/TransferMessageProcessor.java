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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tech.nermindedovic.library.avro.TransferMessage;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
@Profile("avro")
@Slf4j
public class TransferMessageProcessor {
//    @Value("${spring.cloud.stream.kafka.streams.binder.configuration.schema.registry.url}")
    private String schemaRegistryUrl = "http://127.0.0.1:2181";


    private final XmlMapper mapper;
    public TransferMessageProcessor(final XmlMapper mapper) {
        this.mapper = mapper;
    }

    @Bean
    public Serde<TransferMessage> transferMessageSerde() {
        Map<String, String> serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        SpecificAvroSerializer<TransferMessage> serializer = new SpecificAvroSerializer<>();
        SpecificAvroDeserializer<TransferMessage> deserializer = new SpecificAvroDeserializer<>();
        Serde<TransferMessage> transferMessageSerde = Serdes.serdeFrom(serializer, deserializer);
        transferMessageSerde.configure(serdeConfig, false);
        return transferMessageSerde;
    }

    @Bean
    public Function<KStream<String, TransferMessage>, KStream<String, String>> processTransfer() {

        return input -> input
                .mapValues(val -> {
                    log.info(val.toString());
                    try {
                        log.info(mapper.writeValueAsString(val));
                        return mapper.writeValueAsString(val);
                    } catch (JsonProcessingException exception) {
                        exception.printStackTrace();
                        return val.toString();
                    }
                });
    }






}
