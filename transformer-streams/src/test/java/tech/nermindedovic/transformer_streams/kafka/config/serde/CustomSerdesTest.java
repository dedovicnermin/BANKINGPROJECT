package tech.nermindedovic.transformer_streams.kafka.config.serde;


import org.junit.jupiter.api.Test;
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.transformer_streams.kafka.config.serde.json.CustomSerdes;

import static org.assertj.core.api.Assertions.assertThat;

class CustomSerdesTest {



    @Test
    void balanceMessage() {
        CustomSerdes.BalanceMessageSerde balanceMessageSerde = new CustomSerdes.BalanceMessageSerde();
        BalanceMessage balanceMessage = new BalanceMessage(123, 222, "", false);
        byte[] serialize = balanceMessageSerde.serializer().serialize("", balanceMessage);
        BalanceMessage actual = balanceMessageSerde.deserializer().deserialize("", serialize);
        assertThat(actual).isEqualTo(balanceMessage);

        assertThat(balanceMessageSerde.deserializer().deserialize("", null)).isNull();
    }

}