package tech.nermindedovic.rest.kafka.transfer;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import java.util.Map;

public class CustomKafkaAvroSerializer extends KafkaAvroSerializer {
    public CustomKafkaAvroSerializer() {
        super(new MockSchemaRegistryClient());
    }
    public CustomKafkaAvroSerializer(SchemaRegistryClient client) {
        super(client);
    }

    public CustomKafkaAvroSerializer(SchemaRegistryClient client, Map<String, ?> props) {
        super(client, props);
    }

}
