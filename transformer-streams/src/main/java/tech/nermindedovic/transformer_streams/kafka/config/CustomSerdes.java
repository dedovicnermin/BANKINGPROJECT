package tech.nermindedovic.transformer_streams.kafka.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.context.annotation.Bean;
import tech.nermindedovic.transformer_streams.pojos.BalanceMessage;
import tech.nermindedovic.transformer_streams.pojos.TransferMessage;


public final class CustomSerdes {

    public static final class TransferMessageSerde extends Serdes.WrapperSerde<TransferMessage> {
        public TransferMessageSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>(TransferMessage.class));
        }
    }

    public static final class BalanceMessageSerde extends Serdes.WrapperSerde<BalanceMessage> {
        public BalanceMessageSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>(BalanceMessage.class));
        }
    }

    @Bean
    public static Serde<TransferMessage> TransferMessage() {
        return new CustomSerdes.TransferMessageSerde();
    }

    @Bean
    public static Serde<BalanceMessage> BalanceMessage() {
        return new CustomSerdes.BalanceMessageSerde();
    }
}
