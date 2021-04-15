package tech.nermindedovic.transformer.kafka.balance;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.context.annotation.Import;


import org.springframework.kafka.test.context.EmbeddedKafka;

import tech.nermindedovic.transformer.business.pojos.BalanceMessage;
import tech.nermindedovic.transformer.kafka.TransformerProducer;
import tech.nermindedovic.transformer.kafka.TransformerTopicNames;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {TransformerTopicNames.OUTBOUND_PERSISTENCE_BALANCE, TransformerTopicNames.INBOUND_PERSISTENCE_BALANCE})
@Import(BalanceTestConfig.class)
class BalanceMessageIntegrationTest {


   @Autowired
   private TransformerProducer transformerProducer;


   @Test
   void givenValidBalanceMessage_producerWillProduceAndConsumeResponse() {
       BalanceMessage balanceMessage = createBalanceMessage();
       BalanceMessage balanceMessageResponse = transformerProducer.sendAndReceiveBalanceMessage(balanceMessage);
       balanceMessage.setBalance("10.00");
       assertThat(balanceMessageResponse).isEqualTo(balanceMessage);
   }

    private BalanceMessage createBalanceMessage() {
        return new BalanceMessage(123456, 123, "", false);
    }






}
