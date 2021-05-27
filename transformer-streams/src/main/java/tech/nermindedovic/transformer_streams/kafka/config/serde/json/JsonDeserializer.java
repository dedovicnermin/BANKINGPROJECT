package tech.nermindedovic.transformer_streams.kafka.config.serde.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Profile("!avro")
public class JsonDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Class<T> tClass;

    public JsonDeserializer() {}

    public JsonDeserializer(Class<T> tClass) {
        this.tClass = tClass;
    }

    @SneakyThrows
    @Override
    public T deserialize(String s, byte[] bytes) {
        if (bytes == null) return null;
        return objectMapper.readValue(bytes, tClass);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) { /*nothing to do here*/ }

    @Override
    public void close() {/* nothing to do here */}
}
