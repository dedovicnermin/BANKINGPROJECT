package tech.nermindedovic.transformer_streams.kafka.config.serde.avro;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tech.nermindedovic.library.avro.BalanceMessage;


@Profile("avro")
public class BalanceMessageSerde extends SpecificAvroSerializer<BalanceMessage> {
}
