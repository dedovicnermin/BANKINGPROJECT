package tech.nermindedovic.transformer.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import tech.nermindedovic.transformer.kafka.TransformerProducer;
import tech.nermindedovic.transformer.kafka.TransformerTopicNames;
import tech.nermindedovic.transformer.business.pojos.BalanceMessage;
import tech.nermindedovic.transformer.business.pojos.TransferMessage;

@Slf4j
@Service
public class KafkaMessageService {

    // == dependencies ==
    private final TransformerProducer transformerProducer;



    // == fields ==
    private static final String GROUP_ID = "transformer";





    // == constructor ==
    public KafkaMessageService(final TransformerProducer transformerProducer) {
        this.transformerProducer = transformerProducer;
    }


    /**
     *
     * @param balanceMessage object sent by REST application
     * @return balanceMessage response object to REST, after getting a response from Persistence application
     * @throws JsonProcessingException
     * @throws InterruptedException
     */
    @KafkaListener(topics = TransformerTopicNames.INBOUND_REST_BALANCE, containerFactory = "factory", groupId = GROUP_ID)
    @SendTo
    public BalanceMessage listen(final BalanceMessage balanceMessage)  {
        log.info("Transformer received: " + balanceMessage);
        return transformerProducer.sendAndReceiveBalanceMessage(balanceMessage);
    }



    @KafkaListener(topics = TransformerTopicNames.INBOUND_REST_TRANSFER, containerFactory = "transferMessageListenerContainerFactory", groupId = GROUP_ID)
    public void listen(final TransferMessage transferMessage) {
        log.info("Transformer received: " + transferMessage);
        transformerProducer.sendTransferMessage(transferMessage);
    }














}
