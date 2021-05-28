package tech.nermindedovic.transformer_streams.kafka.config.processors.avro;


import org.springframework.cloud.stream.schema.client.ConfluentSchemaRegistryClient;
import org.springframework.cloud.stream.schema.client.SchemaRegistryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("avro")
@Configuration
public class SchemaRegistryConfig {

    private final String endpoint = "http://127.0.0.1:8081";


    @Bean
    public SchemaRegistryClient schemaRegistryClient() {
        ConfluentSchemaRegistryClient client = new ConfluentSchemaRegistryClient();
        client.setEndpoint(endpoint);
        return client;
    }

}
