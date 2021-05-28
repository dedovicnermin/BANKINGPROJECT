package tech.nermindedovic.rest.avro.configuration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import tech.nermindedovic.TransferMessage;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class TransferMessageAvroConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String BROKER;

    @Bean
    @Primary
    public Map<String, Object> transferProducerConfigTest() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomAvroSerializer.class);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 15);
        return props;
    }

    @Bean
    @Primary
    public ProducerFactory<String, TransferMessage> transferProducerFactoryTest() {
        return new DefaultKafkaProducerFactory<>(transferProducerConfigTest());
    }

    @Bean
    @Primary
    @Qualifier("transferTemplate-test")
    public KafkaTemplate<String, TransferMessage> transferMessageKafkaTemplateTest() {
        return new KafkaTemplate<>(transferProducerFactoryTest());
    }


}
