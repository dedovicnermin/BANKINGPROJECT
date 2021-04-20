package tech.nermindedovic.rest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1)
@DirtiesContext
class RestApplicationTests {

	@Test
	void contextLoads() {
	}

}
//TransferMessageIntegrationTest
//, topics = {"funds.transfer.error","funds.transformer.request","balance.transformer.response","balance.transformer.request"}