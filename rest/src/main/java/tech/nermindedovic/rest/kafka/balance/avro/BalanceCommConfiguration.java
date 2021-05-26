package tech.nermindedovic.rest.kafka.balance.avro;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import tech.nermindedovic.library.avro.BalanceMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@Profile("avro")
public class BalanceCommConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String BROKER;

    @Value("${spring.kafka.consumer.group-id}")
    private String GROUP;

    @Bean
    public Map<String, Object> balanceProducerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        configs.put(ProducerConfig.RETRIES_CONFIG, 15);
        return configs;
    }

    @Bean
    public ProducerFactory<String, BalanceMessage> balanceProducerFactory() {
        return new DefaultKafkaProducerFactory<>(balanceProducerConfigs());
    }



    @Bean
    public KafkaMessageListenerContainer<String, BalanceMessage> balanceReplyContainer(ConsumerFactory<String, BalanceMessage> consumerFactory) {
        ContainerProperties containerProperties = new ContainerProperties("balance.transformer.response");
        containerProperties.setGroupId(GROUP);
        Properties properties = new Properties();
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        containerProperties.setKafkaConsumerProperties(properties);
        return new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    }


    @Bean
    public ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> balanceMessageReplyingKafkaTemplate(ProducerFactory<String, BalanceMessage> pf, KafkaMessageListenerContainer<String, BalanceMessage> container) {
        return new ReplyingKafkaTemplate<>(pf, container);
    }



}
