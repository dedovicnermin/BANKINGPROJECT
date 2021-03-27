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
import org.springframework.kafka.listener.GenericMessageListener;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import tech.nermindedovic.transformer.pojos.BalanceMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {


    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String BROKER;

    @Value("${spring.kafka.consumer.group-id}")
    private String GROUP_ID;








    /**
     *
     * CONFIG FOR REST -> TRANSFORMER COMMUNICATION
     * REST sending a BalanceMessage over. Once Transformer gets a response from Persistence,
     * Transformer will reply to REST with the response handed to transformer from persistence.
     */


    /**
     * Consumer configuration.
     * Rest will be sending a replyingKafkaTemplate to our consumer.
     * Have in set up consumer to deserialize the BalanceMessage for it to be processed and returned
     *
     */

    @Bean
    public Map<String, Object> balanceRequestFromRestConsumerConfig() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return configs;
    }





    @Bean
    public ConsumerFactory<String, BalanceMessage> balanceMessageRESTRequestConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(balanceRequestFromRestConsumerConfig());
    }



    /**
     *
     * Spits out containers for methods with @KafkaListener
     * Container expects a BalanceMessage to be sent over from REST app
     * Set reply template for returning response to REST
     */

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> concurrentKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setReplyTemplate(balanceRequestResponseToREST());
        factory.setConsumerFactory(balanceMessageRESTRequestConsumerFactory());
        return factory;
    }




//    @Bean
//    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, BalanceMessage>> requestFromRESTListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(balanceMessageRESTRequestConsumerFactory());
//        factory.setReplyTemplate(balanceRequestResponseToREST());
//
//        return factory;
//    }





    @Bean
    public KafkaMessageListenerContainer<String, BalanceMessage> restReplyContainer(ConsumerFactory<String, BalanceMessage> cf) {
        ContainerProperties containerProperties = new ContainerProperties("balance.transformer.request");
        containerProperties.setGroupId("transformer");
        return new KafkaMessageListenerContainer<>(cf, containerProperties);


    }






    @Bean
    public Map<String, Object> producerReplyingToRestConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return configs;
    }




    @Bean
    public ProducerFactory<String, BalanceMessage> restBalanceResponseProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerReplyingToRestConfigs());
    }



    @Bean
    public KafkaTemplate<String, BalanceMessage>  balanceRequestResponseToREST() {
        return new KafkaTemplate<>(restBalanceResponseProducerFactory());
    }















}
