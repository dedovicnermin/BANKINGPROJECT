//package tech.nermindedovic.transformer.kafka;
//
//
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.listener.ContainerProperties;
//import org.springframework.kafka.listener.KafkaMessageListenerContainer;
//import org.springframework.kafka.support.serializer.JsonDeserializer;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//import tech.nermindedovic.transformer.pojos.BalanceMessage;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class JsonConsumerConfiguration {
//
//
//    /**
//     * Expecting key to be string and value to be json pojo
//     * @return jsonConsumer configuration properties
//     */
//
//    @Bean
//    public Map<String, Object> json_consumerConfiguration() {
//        Map<String, Object> configs = new HashMap<>();
//        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "transformer");
//        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        return configs;
//    }
//
//
//    /**
//     * CONFIGURATION SPECIFIC TO BALANCEMESSAGE
//     */
//
//
//    @Bean
//    public ConsumerFactory<String, BalanceMessage> bm_jsonConsumerFactory() {
//        return new DefaultKafkaConsumerFactory<>(json_consumerConfiguration());
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> bm_jsonKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
//        containerFactory.setConsumerFactory(bm_jsonConsumerFactory());
//        return containerFactory;
//    }
//
//    @Bean
//    public KafkaMessageListenerContainer<String, BalanceMessage> bm_jsonMessageListenerReplyContainer(ConsumerFactory<String, BalanceMessage> cf) {
//        ContainerProperties containerProperties = new ContainerProperties("balance.transformer.response");
//        return new KafkaMessageListenerContainer<>(cf, containerProperties);
//    }
//
//    @Bean
//    public KafkaTemplate<String, BalanceMessage> balanceMessageKafkaTemplate() {
//        Map<String, Object> producerConfigs = new HashMap<>();
//        producerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        producerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs));
//    }
//
//
//
//
//
//
//}
