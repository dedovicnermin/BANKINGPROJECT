package tech.nermindedovic.rest.kafka.balance;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import tech.nermindedovic.rest.api.RestAPI;
import tech.nermindedovic.rest.business.domain.BalanceMessage;



import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers")
@EmbeddedKafka(partitions = 1, topics = {BalanceMessageIntegrationTest.TO_TRANSFORMER, BalanceMessageIntegrationTest.FROM_TRANSFORMER})
@Profile("test")
@ActiveProfiles("test")
@DirtiesContext
@Import(BalanceTestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BalanceMessageIntegrationTest {


    public static final String TO_TRANSFORMER = "balance.transformer.request";
    public static final String FROM_TRANSFORMER = "balance.transformer.response";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private RestAPI restAPI;

    @AfterAll
    void destroyKafka() {
        embeddedKafkaBroker.destroy();
    }


    /**
     * Using BalanceTestConfig for listening + consuming + replying
     */

    @Test
    void givenVALIDBalanceMessage_willSendAndReturnResponseFromTransformer()  {
        BalanceMessage balanceMessage = new BalanceMessage(1111,2222,"", false);
        BalanceMessage returned = restAPI.getBalanceUpdate(balanceMessage);
        balanceMessage.setBalance("10.00");
        assertThat(returned).isEqualTo(balanceMessage);

    }



}
