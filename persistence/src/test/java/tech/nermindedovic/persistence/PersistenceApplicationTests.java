package tech.nermindedovic.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import tech.nermindedovic.persistence.kafka.PersistenceTopicNames;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1)
@DirtiesContext
class PersistenceApplicationTests {


	@Test
	void contextLoads() {

	}

}
//, topics = {PersistenceTopicNames.INBOUND_TRANSFER_REQUEST, PersistenceTopicNames.OUTBOUND_TRANSFER_ERRORS, PersistenceTopicNames.INBOUND_BALANCE_REQUEST, "balance.update.response"}
