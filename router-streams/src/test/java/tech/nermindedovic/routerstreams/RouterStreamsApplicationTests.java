package tech.nermindedovic.routerstreams;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {"spring.autoconfigure.exclude="
		+ "org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
		"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}",
})
@EmbeddedKafka
@DirtiesContext
class RouterStreamsApplicationTests {

	@Test
	void contextLoads() {
		Assertions.assertDoesNotThrow(() -> RouterStreamsApplication.main(new String[] {}));
	}

}
