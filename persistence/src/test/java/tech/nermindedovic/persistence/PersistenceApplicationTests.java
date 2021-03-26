package tech.nermindedovic.persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PersistenceApplicationTests {

	@Autowired
	PersistenceApplication application;
	@Test
	void contextLoads() {
		Assertions.assertNotNull(application);
	}

}
