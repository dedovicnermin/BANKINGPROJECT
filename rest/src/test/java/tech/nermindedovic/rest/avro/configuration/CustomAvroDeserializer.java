package tech.nermindedovic.rest.avro.configuration;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.Schema;
import tech.nermindedovic.library.avro.BalanceMessage;
import tech.nermindedovic.TransferMessage;
import tech.nermindedovic.rest.Topics;


public class CustomAvroDeserializer extends KafkaAvroDeserializer {

    @Override
    public Object deserialize(String topic, byte[] bytes) {
        if (topic.equals(Topics.TRANSFER_OUTBOUND)) {
            this.schemaRegistry = getMockClient(TransferMessage.SCHEMA$);
        } else {
            this.schemaRegistry = getMockClient(BalanceMessage.SCHEMA$);
        }
        return super.deserialize(topic, bytes);
    }

    private static SchemaRegistryClient getMockClient(final Schema schema$) {
       return new MockSchemaRegistryClient() {
           @Override
           public synchronized Schema getById(int id) {
               return schema$;
           }
       };
    }
}
