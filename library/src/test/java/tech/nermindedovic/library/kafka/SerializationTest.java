package tech.nermindedovic.library.kafka;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class SerializationTest {

    @Test
    void deserializerTest() {
        JsonDeserializer<String> deserializer = new JsonDeserializer<>();
        assertThat(deserializer).isNotNull();

        Map<String, ?> configs = new HashMap<>();

        Assertions.assertDoesNotThrow(() -> deserializer.configure(configs, false));

        Assertions.assertDoesNotThrow(deserializer::close);

    }

    @Test
    void serializerTest() {
        JsonSerializer<String> serializer = new JsonSerializer<>();
        assertThat(serializer).isNotNull();

        Map<String, ?> configs = new HashMap<>();
        Assertions.assertDoesNotThrow(() -> serializer.configure(configs, false));
        Assertions.assertDoesNotThrow(serializer::close);

    }
}