package tech.nermindedovic.rest.kafka.transfer;



import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import tech.nermindedovic.AvroBalanceMessage;
import tech.nermindedovic.AvroTransferMessage;
import tech.nermindedovic.library.pojos.BalanceMessage;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@TestConfiguration
@Profile("integration")
public class MockSerdeConfig {



    /**
     * Mock schema registry bean used by Kafka Avro Serde since the
     * EmbeddedKafka setup doesnt include a schema registry.
     * @return MockSchemaRegistryClient instance
     */
    @SneakyThrows
    @Bean
    @Primary
    MockSchemaRegistryClient schemaRegistryClient()  {
        return new MockSchemaRegistryClient();
    }



    @Value("${spring.kafka.bootstrap-servers}")
    private String BROKER;

    @Bean
    @Primary
    public Map<String,Object> transferProducerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomKafkaAvroSerializer.class);
        props.put("schema.registry.url", "not-used");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 15);
        return props;
    }


    @Bean
    @Primary
    public ProducerFactory<String, AvroTransferMessage> transferProducerFactory() {
        return new DefaultKafkaProducerFactory<>(transferProducerConfig());
    }

    @Bean
    @Primary
    @Qualifier("transferTemplate")
    public KafkaTemplate<String, AvroTransferMessage> transferMessageKafkaTemplate() {
        return new KafkaTemplate<>(transferProducerFactory());
    }

//balance

    @Bean
    @Primary
    public Map<String, Object> balanceProducerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomKafkaAvroSerializer.class);
        configs.put("schema.registry.url", "not-used");
        configs.put(ProducerConfig.RETRIES_CONFIG, 15);
        return configs;
    }


    @Bean
    @Primary
    public ProducerFactory<String, AvroBalanceMessage> balanceProducerFactory() {
        return new DefaultKafkaProducerFactory<>(balanceProducerConfigs());
    }


    @Bean
    @Primary
    public ConsumerFactory<String, BalanceMessage> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "rest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(BalanceMessage.class));
    }


    @Bean
    @Primary
    public KafkaMessageListenerContainer<String, BalanceMessage> balanceReplyContainer(ConsumerFactory<String, BalanceMessage> consumerFactory) {
        ContainerProperties containerProperties = new ContainerProperties("balance.transformer.response");
        containerProperties.setGroupId("rest");
        Properties properties = new Properties();
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BalanceMessage.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        containerProperties.setKafkaConsumerProperties(properties);
        return new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    }


    @Bean
    @Primary
    public ReplyingKafkaTemplate<String, AvroBalanceMessage, BalanceMessage> balanceMessageReplyingKafkaTemplate(ProducerFactory<String, AvroBalanceMessage> pf, KafkaMessageListenerContainer<String, BalanceMessage> container) {
        return new ReplyingKafkaTemplate<>(pf, container);
    }







}
