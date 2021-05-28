package tech.nermindedovic.transformer_streams.kafka.config.processors.avro;



import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tech.nermindedovic.library.avro.BalanceMessage;
import tech.nermindedovic.TransferMessage;

import java.util.Collections;
import java.util.Map;

@Profile("avro")
@Configuration
public class AvroSerdesConfig {

    @Value("${spring.cloud.stream.kafka.streams.binder.configuration.schema.registry.url:localhost:8081}")
    private String endpoint;
    private final Map<String, String> config = Collections.singletonMap("schema.registry.url", endpoint);


    @Bean
    public Serde<TransferMessage> transferMessageSerde() {
        SpecificAvroSerializer<TransferMessage> serializer = new SpecificAvroSerializer<>();
        SpecificAvroDeserializer<TransferMessage> deserializer = new SpecificAvroDeserializer<>();
        Serde<TransferMessage> transferMessageSerde = Serdes.serdeFrom(serializer, deserializer);
        transferMessageSerde.configure(config, false);
        return transferMessageSerde;
    }


    @Bean
    public Serde<BalanceMessage> balanceMessageSerde() {
        SpecificAvroSerializer<BalanceMessage> serializer = new SpecificAvroSerializer<>();
        SpecificAvroDeserializer<BalanceMessage> deserializer = new SpecificAvroDeserializer<>();
        Serde<BalanceMessage> balanceMessageSerde = Serdes.serdeFrom(serializer,deserializer);
        balanceMessageSerde.configure(config, false);
        return balanceMessageSerde;
    }



}
