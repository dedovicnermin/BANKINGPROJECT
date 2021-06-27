package tech.nermindedovic.rest.kafka.balance;


import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import tech.nermindedovic.AvroBalanceMessage;
import tech.nermindedovic.library.pojos.BalanceMessage;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@Slf4j
public class BalanceCommConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String BROKER;

    @Value("${spring.kafka.consumer.group-id}")
    private String GROUP;

    @Value("${spring.kafka.properties.schema.registry.url:http://127.0.0.1:8081}")
    private String schemaRegistry;

    @Value("${ssl-enabled}")
    private Boolean sslEnabled;

    @Bean
    public Map<String, Object> balanceProducerConfigs() {

        Map<String, Object> configs = new HashMap<>();
        log.info(BROKER);
        log.info("HEY");
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        configs.put("schema.registry.url", schemaRegistry);
        configs.put(ProducerConfig.RETRIES_CONFIG, 15);
        if (sslEnabled) {
            configs.put("security.protocol", "SSL");
            configs.put("ssl.truststore.location", "/tmp/kafka.client.truststore.jks");
            configs.put("ssl.truststore.password", "123456");
            configs.put("ssl.key.password", "123456");
            configs.put("ssl.keystore.password", "123456");
            configs.put("ssl.keystore.location", "/tmp/kafka.client.keystore.jks");
            configs.put("ssl.endpoint.identification.algorithm", "");
        }
        return configs;
    }

    @Bean
    public ProducerFactory<String, AvroBalanceMessage> balanceProducerFactory() {
        return new DefaultKafkaProducerFactory<>(balanceProducerConfigs());
    }


    @Bean
    public ConsumerFactory<String, BalanceMessage> consumerFactory() {
        log.info(BROKER);
        log.info("HEY");
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        if (sslEnabled) {
            config.put("security.protocol", "SSL");
            config.put("ssl.truststore.location", "file:///tmp/kafka.client.truststore.jks");
            config.put("ssl.truststore.password", "123456");
            config.put("ssl.key.password", "123456");
            config.put("ssl.keystore.password", "123456");
            config.put("ssl.keystore.location", "/tmp/kafka.client.keystore.jks");
            config.put("ssl.endpoint.identification.algorithm", "");
        }


        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(BalanceMessage.class));
    }

    @Bean
    public KafkaMessageListenerContainer<String, BalanceMessage> balanceReplyContainer(ConsumerFactory<String, BalanceMessage> consumerFactory) {
        ContainerProperties containerProperties = new ContainerProperties("balance.transformer.response");
        containerProperties.setGroupId(GROUP);
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BalanceMessage.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        if (sslEnabled) {
            properties.put("security.protocol", "SSL");
            properties.put("ssl.truststore.location", "/tmp/kafka.client.truststore.jks");
            properties.put("ssl.truststore.password", "123456");
            properties.put("ssl.key.password", "123456");
            properties.put("ssl.keystore.password", "123456");
            properties.put("ssl.keystore.location", "/tmp/kafka.client.keystore.jks");
            properties.put("ssl.endpoint.identification.algorithm", "");
        }

        containerProperties.setKafkaConsumerProperties(properties);
        return new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    }


    @Bean
    public ReplyingKafkaTemplate<String, AvroBalanceMessage, BalanceMessage> balanceMessageReplyingKafkaTemplate(ProducerFactory<String, AvroBalanceMessage> pf, KafkaMessageListenerContainer<String, BalanceMessage> container) {
        return new ReplyingKafkaTemplate<>(pf, container);
    }



}
