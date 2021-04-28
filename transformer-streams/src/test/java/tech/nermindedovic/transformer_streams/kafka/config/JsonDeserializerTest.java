package tech.nermindedovic.transformer_streams.kafka.config;


import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;


class JsonDeserializerTest {

    @Test
    void deserializerTest() {
        JsonDeserializer<String> deserializer = new JsonDeserializer<>();
        assertThat(deserializer).isNotNull();

    }

}