package tech.nermindedovic.library.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JsonSerializer<T> implements Serializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public byte[] serialize(String s, T data) {
        if (data == null) return new byte[]{};
        return objectMapper.writeValueAsBytes(data);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {/* nothing to do here */ }

    @Override
    public void close() {/* nothing to do here */ }
}
