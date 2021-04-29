package tech.nermindedovic.transformer_streams;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;


@Slf4j
@SpringBootTest(properties = {"spring.autoconfigure.exclude="
		+ "org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
		"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}",
})
@EmbeddedKafka
@DirtiesContext
class TransformerStreamsApplicationTests {


	@Test
	void contextLoads() {
		Assertions.assertDoesNotThrow(() -> TransformerStreamsApplication.main(new String[] {}));
	}

}
