package tech.nermindedovic.routerstreams;



import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;


import java.time.Duration;


@SpringBootTest(properties = {"spring.autoconfigure.exclude="
		+ "org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
		"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}",
})
@EmbeddedKafka
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RouterStreamsApplicationTests {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	EmbeddedKafkaBroker embeddedKafkaBroker;

	@BeforeAll
	void setConfig() {
		System.setProperty("spring.kafka.bootstrap-servers", embeddedKafkaBroker.getBrokersAsString());
	}

	@Test
	void contextLoads()  {
		Assertions.assertDoesNotThrow(() -> RouterStreamsApplication.main(new String[] {}));
	}

}
