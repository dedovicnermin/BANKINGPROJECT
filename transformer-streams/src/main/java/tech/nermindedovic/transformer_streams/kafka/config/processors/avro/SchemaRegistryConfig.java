package tech.nermindedovic.transformer_streams.kafka.config.processors.avro;


import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import org.springframework.cloud.stream.schema.client.ConfluentSchemaRegistryClient;
import org.springframework.cloud.stream.schema.client.SchemaRegistryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import tech.nermindedovic.library.avro.BalanceMessage;
import tech.nermindedovic.TransferMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Profile("avro")
@Configuration
public class SchemaRegistryConfig {

    private final String endpoint = "http://127.0.0.1:8081";
    private final Map<String, String> config = Collections.singletonMap("schema.registry.url", endpoint);


//    @Bean
//    public SchemaRegistryClient schemaRegistryClient() {
//        ConfluentSchemaRegistryClient client = new ConfluentSchemaRegistryClient();
//        client.setEndpoint(endpoint);
//        return client;
//    }

//    @Bean
//    public Serde<TransferMessage> transferMessageSerde() {
//        final SpecificAvroSerde<TransferMessage> transferSerde = new SpecificAvroSerde<>();
//        transferSerde.configure(config, false);
//        return transferSerde;
//    }

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
        final SpecificAvroSerde<BalanceMessage> balanceSerde = new SpecificAvroSerde<>();
        balanceSerde.configure(config, false);
        return balanceSerde;
    }



}
