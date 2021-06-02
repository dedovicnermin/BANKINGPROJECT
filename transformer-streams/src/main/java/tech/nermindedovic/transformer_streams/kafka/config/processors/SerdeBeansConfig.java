package tech.nermindedovic.transformer_streams.kafka.config.processors;



import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.AvroBalanceMessage;
import tech.nermindedovic.AvroTransferMessage;
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.transformer_streams.kafka.config.serde.json.CustomSerdes;

import java.util.Collections;
import java.util.Map;


@Configuration
@Slf4j
public class SerdeBeansConfig {

    @Value("${schema-endpoint:http://localhost:8081}")
    private String schemaEndpoint;


    // AVRO Serdes
    @Bean
    public Serde<AvroTransferMessage> avroTransferMessageSerde() {
        Map<String, String> config = Collections.singletonMap("schema.registry.url", schemaEndpoint);
        SpecificAvroSerializer<AvroTransferMessage> serializer = new SpecificAvroSerializer<>();
        SpecificAvroDeserializer<AvroTransferMessage> deserializer = new SpecificAvroDeserializer<>();
        Serde<AvroTransferMessage> transferMessageSerde = Serdes.serdeFrom(serializer, deserializer);
        transferMessageSerde.configure(config, false);
        return transferMessageSerde;
    }

    @Bean
    public Serde<AvroBalanceMessage> avroBalanceMessageSerde() {
        Map<String, String> config = Collections.singletonMap("schema.registry.url", schemaEndpoint);
        SpecificAvroSerializer<AvroBalanceMessage> serializer = new SpecificAvroSerializer<>();
        SpecificAvroDeserializer<AvroBalanceMessage> deserializer = new SpecificAvroDeserializer<>();
        Serde<AvroBalanceMessage> balanceMessageSerde = Serdes.serdeFrom(serializer,deserializer);
        balanceMessageSerde.configure(config, false);
        return balanceMessageSerde;
    }

    // JSON Serde
    @Bean
    public Serde<BalanceMessage> balanceMessageSerde() {
        return new CustomSerdes.BalanceMessageSerde();
    }


}
