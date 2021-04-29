package tech.nermindedovic.rest.kafka.balance;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.rest.business.domain.BalanceMessage;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = BalanceMessageIntegrationTest.TO_TRANSFORMER)
@Profile("test")
public class BalanceTestConfig {


        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        @Autowired(required = false)
        private EmbeddedKafkaBroker embeddedKafkaBroker;



        @Bean
        public DefaultKafkaProducerFactory<String, BalanceMessage> pf() {
            Map<String, Object> producerConfig = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
            producerConfig.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000L);
            return new DefaultKafkaProducerFactory<>(producerConfig, new StringSerializer(), new JsonSerializer<>());
        }

        @Bean
        public DefaultKafkaConsumerFactory<String, BalanceMessage> cf() {
            Map<String, Object> consumerConfig = KafkaTestUtils.consumerProps("listen-and-return", "false", embeddedKafkaBroker);
            return new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new JsonDeserializer<>(BalanceMessage.class));
        }

    @Bean
    public DefaultKafkaConsumerFactory<String, String> cf1() {
        Map<String, Object> consumerConfig = KafkaTestUtils.consumerProps("listen-and-return", "false", embeddedKafkaBroker);
        return new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new StringDeserializer());
    }

        @Bean
        public KafkaTemplate<String, BalanceMessage> template() {
            return new KafkaTemplate<>(pf());
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, BalanceMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(cf());
            factory.setReplyTemplate(template());
            return factory;
        }

        @KafkaListener(topics = BalanceMessageIntegrationTest.TO_TRANSFORMER, groupId = "REST-balance-test", id = "restBalance1")
        @SendTo
        public BalanceMessage listenForRestToSendBalanceMessage(BalanceMessage balanceMessage) {
            balanceMessage.setBalance("10.00");
            return balanceMessage;
        }






}
