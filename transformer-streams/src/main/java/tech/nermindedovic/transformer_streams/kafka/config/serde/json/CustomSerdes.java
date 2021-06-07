package tech.nermindedovic.transformer_streams.kafka.config.serde.json;

import org.apache.kafka.common.serialization.Serdes;
import tech.nermindedovic.library.kafka.JsonDeserializer;
import tech.nermindedovic.library.kafka.JsonSerializer;
import tech.nermindedovic.library.pojos.BalanceMessage;

public final class CustomSerdes {

    private CustomSerdes() {}


    public static final class BalanceMessageSerde extends Serdes.WrapperSerde<BalanceMessage> {
        public BalanceMessageSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>(BalanceMessage.class));
        }
    }


}
