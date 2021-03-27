package tech.nermindedovic.transformer.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import tech.nermindedovic.transformer.pojos.BalanceMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ConfigBalanceTopic {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String BROKER;

    @Value("${spring.kafka.consumer.group-id}")
    private String GROUP_ID;





    @Bean
    public Map<String, Object> prodConfigForPojo() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return configs;
    }
    @Bean
    public ProducerFactory<String, BalanceMessage> balanceMessageProducerFactory() {
        return new DefaultKafkaProducerFactory<>(prodConfigForPojo());
    }

    @Bean
    public KafkaTemplate<String, BalanceMessage> balanceMessageKafkaTemplate() {
        return new KafkaTemplate<>(balanceMessageProducerFactory());
    }

//    @Bean
//    public ReplyingKafkaTemplate<String, String, String> balanceMessageReplyingKafkaTemplate(ProducerFactory<String, String> pf, KafkaMessageListenerContainer<String, String> container) {
//        return new ReplyingKafkaTemplate<>(pf, container);
//    }

    /**
     * Listener container to be set up in replyingKafkaTemplate
     * @return
     */
//    @Bean
//    public KafkaMessageListenerContainer<String, String> balanceReplyContainer(ConsumerFactory<String, String> consumerFactory) {
//        ContainerProperties containerProperties = new ContainerProperties("balance.update.response");
////        containerProperties.setGroupId("transformer");
//        return new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
//    }



    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(ProducerFactory<String, String> pf,
                                                                               ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        ConcurrentMessageListenerContainer<String, String> replyContainer = factory.createContainer("balance.update.response");
        replyContainer.getContainerProperties().setMissingTopicsFatal(false);
        replyContainer.getContainerProperties().setGroupId("transformer");
        return new ReplyingKafkaTemplate<>(pf, replyContainer);
    }


    @Bean
    public KafkaTemplate<String, String> replyTemplate(ProducerFactory<String, String> pf, ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(pf);
        factory.setReplyTemplate(kafkaTemplate);
        return kafkaTemplate;
    }











    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return configs;
    }



    @Bean
    public Map<String, Object> bm_stringConsumerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return configs;
    }

    @Bean
    public ConsumerFactory<String, String> balanceMessageResponseConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(bm_stringConsumerConfigs());
    }

//    @Bean
//    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> balanceMessageReplyKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(balanceMessageResponseConsumerFactory());
//        factory.setReplyTemplate(balanceKafkaTemplate());
//        return factory;
//    }


    /**
     *
     * @return template responding to BalanceMessage requests
     */
//    @Bean
//    public KafkaTemplate<String, String> balanceKafkaTemplate() {
//        return new KafkaTemplate<>(producerFactory());
//    }

}
