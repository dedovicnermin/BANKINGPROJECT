package tech.nermindedovic.persistence.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.KafkaConsumerTest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import org.springframework.util.concurrent.ListenableFuture;
import tech.nermindedovic.persistence.business.service.ConsumerService;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;

@Disabled

@SpringBootTest
//@DirtiesContext
//@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port"})
public class EmbeddedKafkaIntegrationTest {

//    @Autowired
//    private ConsumerService consumer;
//
//    @Autowired
//    private KafkaTemplate<String, String> template;
//
//    @Value("${balance.request.topic}")
//    private String topic;
//
//    @Test
//    public void givenEmbeddedKafkaBroker_whenSendingtoSimpleProducer_thenMessageReceived() throws ExecutionException, InterruptedException {
//        ListenableFuture<SendResult<String,String>> future = template.send(topic, "message from test");
//
//        String res = future.get().getProducerRecord().value();
//        assertThat(res).isEqualTo("message from test");
//
//    }

}
