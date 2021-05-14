package tech.nermindedovic.rest.kafka.transfer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransferErrorConsumer {



    /**
     * Consumer configs autowired from yaml properties file.
     * @param errorMessage sent from persistence || transformer
     */
    @KafkaListener(topics = "funds.transfer.error", groupId = "rest", id = "dlqListener")
    public void listen(String errorMessage) {
        log.error("Rest error listener: " + errorMessage);
    }

}
