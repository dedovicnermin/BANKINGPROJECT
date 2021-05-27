package tech.nermindedovic.transformer_streams.kafka.config;

import org.apache.kafka.common.serialization.Serde;
import org.junit.jupiter.api.Test;
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;
import tech.nermindedovic.transformer_streams.kafka.config.serde.json.CustomSerdes;


import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CustomSerdesTest {

    @Test
    void transferMessage() {
        CustomSerdes.TransferMessageSerde transferMessageSerde = new CustomSerdes.TransferMessageSerde();
        TransferMessage transferMessage = new TransferMessage(12,new Creditor(1,111), new Debtor(2,222), LocalDate.now(), BigDecimal.TEN, "memo");

        byte[] serialize = transferMessageSerde.serializer().serialize("", transferMessage);
        TransferMessage deserialize = transferMessageSerde.deserializer().deserialize("", serialize);
        assertThat(deserialize).isEqualTo(transferMessage);
        assertThat(transferMessageSerde.deserializer().deserialize("", null)).isNull();

    }

    @Test
    void balanceMessage() {
        CustomSerdes.BalanceMessageSerde balanceMessageSerde = new CustomSerdes.BalanceMessageSerde();
        BalanceMessage balanceMessage = new BalanceMessage(123, 222, "", false);
        byte[] serialize = balanceMessageSerde.serializer().serialize("", balanceMessage);
        BalanceMessage actual = balanceMessageSerde.deserializer().deserialize("", serialize);
        assertThat(actual).isEqualTo(balanceMessage);

        assertThat(balanceMessageSerde.deserializer().deserialize("", null)).isNull();





    }

    @Test
    void transferSerdeTest() {
        Serde<TransferMessage> transferMessageSerde = CustomSerdes.TransferMessage();
        assertThat(transferMessageSerde).isNotNull();

        Serde<BalanceMessage> balanceMessageSerde = CustomSerdes.BalanceMessage();
        assertThat(balanceMessageSerde).isNotNull();
    }
}