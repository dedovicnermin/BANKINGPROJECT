package tech.nermindedovic.persistence.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import tech.nermindedovic.persistence.business.doman.Creditor;
import tech.nermindedovic.persistence.business.doman.Debtor;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.business.service.ConsumerService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;


@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@Slf4j
public class EmbeddedKafkaIntegrationTest {

    @Autowired
    private ConsumerService consumerService;


    @Autowired
    KafkaTemplate<String, String> template;

    @Value("${balance.request.topic}")
    private String topic;

    @Test
    public void givenEmbeddedKafkaBroker_whenSendingtoSimpleProducer_thenMessageReceived() throws ExecutionException, InterruptedException {
        ListenableFuture<SendResult<String,String>> future = template.send(topic, "message from test");
        String res = future.get().getProducerRecord().value();
        assertThat(res).isEqualTo("message from test");
    }



    @Test
    void test_whenGoodXML_allGood() throws JsonProcessingException {
        String xml = mapper.writeValueAsString(new TransferMessage(23, new Creditor(2, 12355534), new Debtor(1, 8435973), new Date(), new BigDecimal("25.00"), "This is my memo found in the kafka embedded test"));
        ListenableFuture<SendResult<String, String>> future = template.send(topic, xml);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.info(throwable.toString());
                log.info("FAIL");
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info("SUCCESS");
                log.info(result.getProducerRecord().value() + " recieved");
            }
        });


        try {
            assertThat(future.get().getProducerRecord().value().equalsIgnoreCase(xml));
        } catch (Exception e) {
            Assertions.fail();
        }
        assertThat(future.isDone());



    }

    XmlMapper mapper = new XmlMapper();




}
