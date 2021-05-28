package tech.nermindedovic.rest.avro.configuration;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import tech.nermindedovic.rest.Topics;

import java.util.Map;

public class CustomAvroSerializer extends KafkaAvroSerializer {
    public CustomAvroSerializer() {
        super();
        super.schemaRegistry = new MockSchemaRegistryClient();
    }

    public CustomAvroSerializer(SchemaRegistryClient client) {super(new MockSchemaRegistryClient());}

    public CustomAvroSerializer(SchemaRegistryClient client, Map<String, ?> config) {
        super(new MockSchemaRegistryClient(), config);
    }
}
