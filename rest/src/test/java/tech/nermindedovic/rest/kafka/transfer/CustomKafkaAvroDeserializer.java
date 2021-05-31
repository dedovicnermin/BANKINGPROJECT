package tech.nermindedovic.rest.kafka.transfer;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.Schema;
import tech.nermindedovic.AvroBalanceMessage;
import tech.nermindedovic.AvroTransferMessage;
import tech.nermindedovic.rest.Topics;

import java.util.Map;

public class CustomKafkaAvroDeserializer extends KafkaAvroDeserializer {
    public CustomKafkaAvroDeserializer(SchemaRegistryClient client, Map<String, ?> map) {
        super(client, map);
    }

    @Override
    public Object deserialize(String topic, byte[] bytes) {
        if (topic.equals(Topics.BALANCE_OUTBOUND)) {
            this.schemaRegistry = getMockClient(AvroBalanceMessage.SCHEMA$);
        }
        if (topic.equals(Topics.TRANSFER_OUTBOUND)) {
            this.schemaRegistry = getMockClient(AvroTransferMessage.SCHEMA$);
        }
        return super.deserialize(topic, bytes);
    }



    private SchemaRegistryClient getMockClient(final Schema schema$) {
        return new MockSchemaRegistryClient() {
            @Override
            public synchronized Schema getById(int id) {
                return schema$;
            }
        };
    }
}
