package tech.nermindedovic.persistence.kafka;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.library.pojos.TransferValidation;


import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class ConsumerConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${ssl-enabled}")
    private boolean sslEnabled;


    @Value("${truststore}")
    private String truststoreLocation;

    @Value("${keystore}")
    private String keystoreLocation;

    @Value("${security-password}")
    private String sslPassword;

    private static final String SSL = "SSL";
    private static final String PROTOCOL_PROP = "security.protocol";
    private static final String TRUSTSTORE_LOCATION_PROP = "ssl.truststore.location";
    private static final String KEYSTORE_LOCATION_PROP = "ssl.keystore.location";
    private static final String KEY_PASS_PROP = "ssl.key.password";
    private static final String KEYSTORE_PASS_PROP = "ssl.keystore.password";
    private static final String TRUSTSTORE_PASS_PROP = "ssl.truststore.password";
    private static final String ENDPOINT_IDENTITY_PROP = "ssl.endpoint.identification.algorithm";


    /**
     * Config for consumers
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "${spring.kafka.consumer.groupId}");
        if (sslEnabled) {
            configs.put(PROTOCOL_PROP, SSL);
            configs.put(TRUSTSTORE_LOCATION_PROP, truststoreLocation);
            configs.put(TRUSTSTORE_PASS_PROP, sslPassword);
            configs.put(KEY_PASS_PROP, sslPassword);
            configs.put(KEYSTORE_PASS_PROP, sslPassword);
            configs.put(KEYSTORE_LOCATION_PROP, keystoreLocation);
            configs.put(ENDPOINT_IDENTITY_PROP, "");
        }

        return configs;
    }

    /**
     * consumer factory for both containers.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configs = consumerConfigs();
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    /**
     * ConcurrentKafkaListenerContainerFactory to create containers for methods annotated with @KafkaListener
     * Contains container config for transfer message listener
     *
     * container props .setAckOnError defaults to true.
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());
        factory.setErrorHandler(new KafkaErrHandler(kafkaTemplate()));
        return factory;
    }


    /**
     * Listener container config for nonReplying consumer - funds transfer.
     * Ack on successful processing
     */

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> nonReplying_ListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setErrorHandler(new KafkaErrHandler(kafkaTemplate()));
        return factory;
    }



    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        Map<String, Object> templateConfig = getTemplateConfig();
        templateConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(templateConfig));
    }




    @Bean
    public ConsumerFactory<String, TransferValidation> validationConsumerFactory() {
        Map<String, Object> configs = consumerConfigs();
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), new JsonDeserializer<>(TransferValidation.class));
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, TransferValidation>> validationListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransferValidation> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(validationConsumerFactory());
        factory.setReplyTemplate(validationKafkaTemplate());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }



    @Bean
    public KafkaTemplate<String, TransferValidation> validationKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(getTemplateConfig()));
    }

    @Bean
    public KafkaTemplate<String, TransferStatus> statusKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(getTemplateConfig()));
    }


    private Map<String,Object> getTemplateConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.RETRIES_CONFIG, 5);
        return config;
    }




}
