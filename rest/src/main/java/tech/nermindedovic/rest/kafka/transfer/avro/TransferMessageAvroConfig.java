package tech.nermindedovic.rest.kafka.transfer.avro;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import tech.nermindedovic.TransferMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("avro")
public class TransferMessageAvroConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String BROKER;

    @Value("${schema-registry-endpoint:http://127.0.0.1:8081}")
    private String schemaRegistry;

    @Bean
    public Map<String, Object> transferProducerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", schemaRegistry);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 15);
        return props;
    }


    @Bean
    public ProducerFactory<String, TransferMessage> transferProducerFactory() {
        return new DefaultKafkaProducerFactory<>(transferProducerConfig());
    }



    @Bean
    @Qualifier("transferTemplate")
    public KafkaTemplate<String, TransferMessage> transferMessageKafkaTemplate() {
        return new KafkaTemplate<>(transferProducerFactory());
    }


}
