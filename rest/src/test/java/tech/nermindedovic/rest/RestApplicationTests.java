package tech.nermindedovic.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {"funds.transfer.error","funds.transformer.request","balance.transformer.response","balance.transformer.request"})
@DirtiesContext
class RestApplicationTests {

	@Test
	void contextLoads() {
		Assertions.assertDoesNotThrow(() -> RestApplication.main(new String[] {}));
	}

}
