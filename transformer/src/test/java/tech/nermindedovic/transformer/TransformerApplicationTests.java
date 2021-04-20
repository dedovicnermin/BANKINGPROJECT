package tech.nermindedovic.transformer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;


@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1)
@DirtiesContext
class TransformerApplicationTests {



	@Test
	void contextLoads() {
	}




}
//, topics = {TransformerTopicNames.INBOUND_PERSISTENCE_BALANCE, TransformerTopicNames.INBOUND_REST_BALANCE, TransformerTopicNames.INBOUND_REST_TRANSFER, TransformerTopicNames.OUTBOUND_PERSISTENCE_BALANCE, TransformerTopicNames.OUTBOUND_PERSISTENCE_TRANSFER, TransformerTopicNames.OUTBOUND_TRANSFER_ERRORS}