package tech.nermindedovic.library.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class SerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializerTest() throws JsonProcessingException {
        JsonDeserializer<TransferMessage> deserializer = new JsonDeserializer<>(TransferMessage.class);
        assertThat(deserializer).isNotNull();

        Map<String, ?> configs = new HashMap<>();
        Assertions.assertDoesNotThrow(() -> deserializer.configure(configs, false));
        Assertions.assertDoesNotThrow(deserializer::close);

        TransferMessage expected = new TransferMessage(1, new Creditor(1, 111), new Debtor(2, 222), LocalDate.now(), BigDecimal.ONE, "Test memo");
        byte[] bytes = mapper.writeValueAsBytes(expected);
        assertThat(deserializer.deserialize("topic1", bytes)).isEqualTo(expected);


        JsonDeserializer<String> deserializer1 = new JsonDeserializer<>();
        assertThat(deserializer1.deserialize("empty", null)).isNull();

    }

    @Test
    void serializerTest() throws JsonProcessingException {
        JsonSerializer<String> serializer = new JsonSerializer<>();
        assertThat(serializer).isNotNull();

        Map<String, ?> configs = new HashMap<>();
        Assertions.assertDoesNotThrow(() -> serializer.configure(configs, false));
        Assertions.assertDoesNotThrow(serializer::close);

        String input = "MockValue";
        byte[] expected = mapper.writeValueAsBytes(input);

        assertThat(serializer.serialize("empty", null)).isEmpty();
        assertThat(serializer.serialize("topic", input)).isEqualTo(expected);

    }



}