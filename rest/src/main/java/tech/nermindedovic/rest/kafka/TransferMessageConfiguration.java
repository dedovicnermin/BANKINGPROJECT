package tech.nermindedovic.rest.kafka;


import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import tech.nermindedovic.rest.business.domain.TransferMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TransferMessageConfiguration {



    @Bean
    public Map<String, Object> transferProducerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
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
