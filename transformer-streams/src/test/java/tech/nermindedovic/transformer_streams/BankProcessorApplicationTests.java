package tech.nermindedovic.transformer_streams;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;


@Slf4j
@SpringBootTest(properties = "spring.cloud.stream.kafka.streams.binder.brokers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka
class BankProcessorApplicationTests {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;




	@Test
	void contextLoads() {
		log.info(embeddedKafkaBroker.getBrokersAsString());

	}

}
