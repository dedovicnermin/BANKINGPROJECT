package tech.nermindedovic.transformer.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import tech.nermindedovic.transformer.kafka.TransformerProducer;
import tech.nermindedovic.transformer.pojos.BalanceMessage;
import tech.nermindedovic.transformer.pojos.TransferMessage;

@Slf4j
@Service
public class KafkaMessageService {

    // == dependencies ==
    private final TransformerProducer transformerProducer;



    // == fields ==
    private static final String GROUP_ID = "transformer";
    private static final String REQ_FROM_REST_TOPIC = "balance.transformer.request";




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
    @KafkaListener(topics = "balance.transformer.request", containerFactory = "factory", groupId = GROUP_ID)
    @SendTo
    public BalanceMessage listen(final BalanceMessage balanceMessage)  {
        log.info("Transformer received: " + balanceMessage);
        return transformerProducer.sendAndReceiveBalanceMessage(balanceMessage);
    }



    @KafkaListener(topics = "funds.transformer.request", containerFactory = "transferMessageListenerContainerFactory", groupId = GROUP_ID)
    public void listen(final TransferMessage transferMessage) {
        log.info("Transformer received: " + transferMessage);
        transformerProducer.sendTransferMessage(transferMessage);
    }














}
