package tech.nermindedovic.rest;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;


import java.time.Duration;

@SpringBootTest(classes = RestApplication.class)
@EmbeddedKafka(partitions = 1, controlledShutdown = true)
@DirtiesContext
class ContextLoadsTest {

    @Test
    void contextLoads() {
        Assertions.assertTimeout(Duration.ofSeconds(5), () -> RestApplication.main(new String[] {}));
    }

}
