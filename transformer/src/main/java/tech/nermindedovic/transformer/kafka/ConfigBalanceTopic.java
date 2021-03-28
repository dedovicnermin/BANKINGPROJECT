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

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String BROKER;


    private static final String GROUP_ID = "transformer";





    @Bean
    public Map<String, Object> prodConfigForPojo() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
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





    @Bean
    public ConsumerFactory<String, BalanceMessage> consumerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");


        return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), new JsonDeserializer<>(BalanceMessage.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> factory() {
        ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setGroupId(GROUP_ID);
        factory.setReplyTemplate(balanceMessageKafkaTemplate());

        return factory;
    }







    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(ProducerFactory<String, String> pf,
                                                                               ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        ConcurrentMessageListenerContainer<String, String> replyContainer = factory.createContainer("balance.update.response");
        replyContainer.getContainerProperties().setMissingTopicsFatal(false);
        replyContainer.getContainerProperties().setGroupId(GROUP_ID);
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


}
