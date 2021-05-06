package tech.nermindedovic.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {"funds.transfer.error","funds.transformer.request","balance.transformer.response","balance.transformer.request"})
@DirtiesContext
@ActiveProfiles("default")
class RestApplicationTests {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@Test
	void contextLoads() {
		Assertions.assertDoesNotThrow(() -> RestApplication.main(new String[] {}));
	}


}
