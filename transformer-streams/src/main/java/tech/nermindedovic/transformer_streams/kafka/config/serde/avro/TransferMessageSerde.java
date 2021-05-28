package tech.nermindedovic.transformer_streams.kafka.config.serde.avro;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tech.nermindedovic.library.avro.TransferMessage;


@Profile("avro")
public class TransferMessageSerde extends SpecificAvroSerde<TransferMessage> {
}
