package tech.nermindedovic.persistence.kafka;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hibernate.exception.JDBCConnectionException;
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
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;


import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class ConsumerConfiguration {


    /**
     * Config for balance
     * Shouldnt need to specify bootstrap servers with config in application.yaml
     *
     * @return
     */
    @Bean
    public Map<String, Object> balance_consumerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return configs;
    }

    /**
     * pops out consumers
     * @return
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(balance_consumerConfigs());
    }

    /**
     * ConcurrentKafkaListenerCOntainerFactory to create containers for methods annotated with @KafkaListener
     * KafkaListenerContainer recieves all the messages from my topics on a single thread.
     *
     * container props .setAckOnError defaults to true.
     * @return
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        return factory;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configs));
    }


    /**
     * Listener container factory for nonReplying consumer - funds transfer.
     * Manual acknowledgment
     * @return
     */

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> nonReplying_ListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);



        return factory;
    }


    //        factory.setErrorHandler((thrownException, data) -> {
//            log.error("Exception in consumerConfig is {} and the record is {}", thrownException.getMessage(), data);
//            kafkaTemplate().send("", data.toString());
//        });
//
//        factory.setRetryTemplate(retryTemplate());
//        factory.setRecoveryCallback((context -> {
//            if (context.getLastThrowable().getCause() instanceof JDBCConnectionException){
//                //recovery logic
//
//            } else {
//                log.error("Inside the non recoverable logic");
//                throw new InvalidTransferMessageException(context.getLastThrowable().getMessage());
//            }
//            return null;
//        }));

//    @Bean
//    public RetryTemplate retryTemplate() {
//        RetryTemplate retryTemplate = new RetryTemplate();
//        retryTemplate().setRetryPolicy(simpleRetryPolicy());
//        return  retryTemplate;
//    }
//
//
//    @Bean
//    public RetryPolicy simpleRetryPolicy() {
//        Map<Class<? extends Throwable>, Boolean> exceptionsMap = new HashMap<>();
//        exceptionsMap.put(InvalidTransferMessageException.class, false);
//        exceptionsMap.put(JDBCConnectionException.class, true);
//        exceptionsMap.put(SQLException.class, false);
//        return new SimpleRetryPolicy(5, exceptionsMap, true);
//    }







    




}
