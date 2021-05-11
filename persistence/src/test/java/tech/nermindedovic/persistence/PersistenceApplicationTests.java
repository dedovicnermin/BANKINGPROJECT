package tech.nermindedovic.persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;


import java.time.Duration;


@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
@DirtiesContext
class PersistenceApplicationTests {

	@Test
	void contextLoads() {
		Assertions.assertTimeout(Duration.ofSeconds(10),() -> PersistenceApplication.main(new String[]{}));
	}

}

