package tech.nermindedovic.rest.kafka.balance;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import tech.nermindedovic.rest.business.domain.BalanceMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class BalanceCommConfiguration {

    @Bean
    public Map<String, Object> balance_producerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(ProducerConfig.RETRIES_CONFIG, 15);
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);


        return configs;
    }

    @Bean
    public ProducerFactory<String, BalanceMessage> balance_producerFactory() {
        return new DefaultKafkaProducerFactory<>(balance_producerConfigs());
    }



    @Bean
    public KafkaMessageListenerContainer<String, BalanceMessage> balanceReplyContainer(ConsumerFactory<String, BalanceMessage> consumerFactory) {
        ContainerProperties containerProperties = new ContainerProperties("balance.transformer.response");
        containerProperties.setGroupId("rest");
        Properties properties = new Properties();
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BalanceMessage.class);

        containerProperties.setKafkaConsumerProperties(properties);

        return new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    }


    @Bean
    public ReplyingKafkaTemplate<String, BalanceMessage, BalanceMessage> balanceMessageReplyingKafkaTemplate(ProducerFactory<String, BalanceMessage> pf, KafkaMessageListenerContainer<String, BalanceMessage> container) {
        return new ReplyingKafkaTemplate<>(pf, container);
    }



}
