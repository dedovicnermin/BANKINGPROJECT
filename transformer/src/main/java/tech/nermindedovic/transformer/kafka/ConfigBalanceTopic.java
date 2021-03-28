package tech.nermindedovic.transformer.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import tech.nermindedovic.transformer.pojos.BalanceMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ConfigBalanceTopic {

    @Value("${spring.kafka.bootstrap-servers}")
    private String BROKER;

    @Value("${spring.kafka.consumer.group-id}")
    private String GROUPID;


    /**
     * Configuration for communication with REST application.
     * Producing JSON representation of BalanceMessage
     * TypeInfoHeaders set to false to leave object type up to the reciever
     * @return producer configuration stored in a HashMap
     */
    @Bean
    public Map<String, Object> prodConfigForPojo() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return configs;
    }


    /**
     * Factory used to produce message templates with specified configuration
     * @return DefaultProducerFactory with JSON specified configuration
     */
    @Bean
    public ProducerFactory<String, BalanceMessage> balanceMessageProducerFactory() {
        return new DefaultKafkaProducerFactory<>(prodConfigForPojo());
    }

    /**
     * Template for replying to Rest message.
     * Not explicitly used in code but will be used by spring under the hood.
     * Infers that this is the template we'll be using in KafkaMessageService.java
     * @return
     */
    @Bean
    public KafkaTemplate<String, BalanceMessage> balanceMessageKafkaTemplate() {
        return new KafkaTemplate<>(balanceMessageProducerFactory());
    }


    /**
     * Consumer factory for the consumption of balance request messages sent from REST application
     * Passing instance of JsonDeserializer with target type BalanceMessage
     * Trust packages to bypass warnings from Kafka
     *
     * @return ConsumerFactory for listeners that are expecting to hear from REST app
     */
    @Bean
    public ConsumerFactory<String, BalanceMessage> consumerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUPID);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");


        return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), new JsonDeserializer<>(BalanceMessage.class));
    }


    /**
     * Configuring listener container factory for messages sent from rest application.
     * ReplyTemplate must be set inorder for the container to execute the reply
     * @return ConcurrentKafkaListenerContainerFactory that handles the creation of MessageListener containers
     *
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> factory() {
        ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setGroupId(GROUPID);
        factory.setReplyTemplate(balanceMessageKafkaTemplate());

        return factory;
    }















    /**
     *  Request/Reply communication with Persistence application
     *
     * @param pf Factory of string:string producers
     * @param factory the container factory that will be expecting a reply once produced message has been processed by persistence application
     * @return ReplyingKafkaTemplate with producer factory and messageListenerContainer configured
     */
    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(ProducerFactory<String, String> pf,
                                                                               ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        ConcurrentMessageListenerContainer<String, String> replyContainer = factory.createContainer("balance.update.response");
        replyContainer.getContainerProperties().setMissingTopicsFatal(false);
        replyContainer.getContainerProperties().setGroupId(GROUPID);
        return new ReplyingKafkaTemplate<>(pf, replyContainer);
    }


    /**
     * Template to expect to receive on persistence response
     * Strapping the kafkaTemplate to the response listener
     * @param pf ProducerFactory
     * @param factory MessageListenerContainerFactory instance from the ReplyingKafkaTemplate
     * @return
     */
    @Bean
    public KafkaTemplate<String, String> replyTemplate(ProducerFactory<String, String> pf, ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(pf);
        factory.setReplyTemplate(kafkaTemplate);
        return kafkaTemplate;
    }


    /**
     * ProducerFactory for communication with persistence
     * @return ProducerFactory of templates that send string:string key:val messages
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }


    /**
     * Specifies the configurations to be used for producer communication with persistence
     * @return Map of producer configuration
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return configs;
    }


    /**
     * Configuration for the expected response from Persistence
     * @return Map of consumer configuration
     */
    @Bean
    public Map<String, Object> bm_stringConsumerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUPID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return configs;
    }


    /**
     * Specifying consumer factory for response messages from persistence
     * @return ConsumerFactory
     */
    @Bean
    public ConsumerFactory<String, String> balanceMessageResponseConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(bm_stringConsumerConfigs());
    }


}
