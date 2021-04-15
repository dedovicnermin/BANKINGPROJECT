package tech.nermindedovic.transformer.kafka.balance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.handler.annotation.SendTo;
import tech.nermindedovic.transformer.business.pojos.BalanceMessage;
import tech.nermindedovic.transformer.kafka.TransformerTopicNames;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@EnableKafka
public class BalanceTestConfig {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Bean
    public DefaultKafkaProducerFactory<String, String> pf() {
        Map<String, Object> producerConfig = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producerConfig.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000L);
        return new DefaultKafkaProducerFactory<>(producerConfig, new StringSerializer(), new StringSerializer());
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, String> cf() {
        Map<String, Object> consumerConfig = KafkaTestUtils.consumerProps("balance-test-config", "false", embeddedKafkaBroker);
        return new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public KafkaTemplate<String, String> template() {
        return new KafkaTemplate<>(pf());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf());
        factory.setReplyTemplate(template());
        return factory;
    }

    @KafkaListener(topics = TransformerTopicNames.OUTBOUND_PERSISTENCE_BALANCE, groupId = "balance-test-config")
    @SendTo
    public String listenForTransformerRequestAndReply(String xml) throws JsonProcessingException {
        XmlMapper mapper = new XmlMapper();
        BalanceMessage balanceMessage = mapper.readValue(xml, BalanceMessage.class);
        balanceMessage.setBalance("10.00");
        return mapper.writeValueAsString(balanceMessage);
    }
}
