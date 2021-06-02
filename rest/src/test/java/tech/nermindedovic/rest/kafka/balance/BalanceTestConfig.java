package tech.nermindedovic.rest.kafka.balance;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.handler.annotation.SendTo;
import tech.nermindedovic.AvroBalanceMessage;
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.rest.Topics;



import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@Profile("integration")
public class BalanceTestConfig {

    @Autowired
    MockSchemaRegistryClient schemaRegistryClient;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;




    @Bean
    public DefaultKafkaProducerFactory<String, BalanceMessage> pf() {
        Map<String, Object> producerConfig = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerConfig.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000L);
        return new DefaultKafkaProducerFactory<>(producerConfig);
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, AvroBalanceMessage> cf() {
        Map<String,Object> consumerConfig = KafkaTestUtils.consumerProps("listen-and-return", "false", embeddedKafkaBroker);
        consumerConfig.put("schema.registry.url", "not-used");
        return new DefaultKafkaConsumerFactory<>(consumerConfig);
    }

    @Bean
    public KafkaTemplate<String, BalanceMessage> template() {
        return new KafkaTemplate<>(pf());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AvroBalanceMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AvroBalanceMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf());
        factory.setReplyTemplate(template());
        return factory;
    }

    @KafkaListener(topics = Topics.BALANCE_OUTBOUND, groupId = "REST-balance-test", id = "restBalance1")
    @SendTo
    public BalanceMessage listenForRestToSendBalanceMessage(AvroBalanceMessage balanceMessage) {
        balanceMessage.setBalance("10.00");
        return new BalanceMessage(balanceMessage.getAccountNumber(), balanceMessage.getRoutingNumber(), balanceMessage.getBalance(), balanceMessage.getErrors());
    }





}
