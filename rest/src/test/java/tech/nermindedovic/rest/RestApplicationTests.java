package tech.nermindedovic.rest;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;




@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {"funds.transfer.error",
		"funds.transformer.request",
		"balance.transformer.response",
		"balance.transformer.request"
})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestApplicationTests {



	@BeforeAll
	void configure() {
		System.setProperty("spring.kafka.bootstrap-servers", embeddedKafkaBroker.getBrokersAsString());
	}

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;



	@Test
	void contextLoads() {
		Assertions.assertDoesNotThrow(() -> RestApplication.main(new String[] {}));
	}


}

