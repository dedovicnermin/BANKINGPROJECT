package tech.nermindedovic.transformer_streams;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.annotation.DirtiesContext;
import java.time.Duration;




@Slf4j
@SpringBootTest
@DirtiesContext
class TransformerStreamsApplicationTests {



	@Test
	void contextLoads() {
		Assertions.assertTimeout(Duration.ofSeconds(5), () -> TransformerStreamsApplication.main(new String[]{}));
	}

}
