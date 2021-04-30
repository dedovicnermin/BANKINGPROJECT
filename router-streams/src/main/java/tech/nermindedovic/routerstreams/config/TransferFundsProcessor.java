package tech.nermindedovic.routerstreams.config;


import lombok.extern.slf4j.Slf4j;


import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.*;

import org.apache.kafka.streams.state.KeyValueStore;
import org.jdom2.JDOMException;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import tech.nermindedovic.routerstreams.utils.RouterJsonMapper;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;
import tech.nermindedovic.routerstreams.business.domain.*;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@Slf4j
public class TransferFundsProcessor {

    public static final String VALIDATE_USER = RouterTopicNames.OUTBOUND_VALIDATION_PREFIX;
    public static final String FUNDS_SINGLE_ACCOUNT = RouterTopicNames.OUTBOUND_FUNDS_SINGLE_ACCOUNT_PREFIX;

    private final StreamBridge streamBridge;
    private  final RouterJsonMapper mapper;
    private final TransferMessageParser parser;
    public TransferFundsProcessor(final StreamBridge streamBridge, final RouterJsonMapper routerJsonMapper, final TransferMessageParser transferMessageParser) {
        this.streamBridge = streamBridge;
        this.mapper = routerJsonMapper;
        this.parser = transferMessageParser;
    }



    /**
     * parse incoming transfer message.
     * Ensure it has information for a payment party object
     * delegate on number of valid routing numbers
     */
    @Bean
    public Consumer<String> consumeInitialTransfer() {
        return transferMessage -> {
            try {
                TransferMessageParser transferMessageParser = parser.build(transferMessage);
                if (transferMessageParser.getPaymentParty().invalidRoutingNumbersPresent()) {
                    sendToErrorTopic(transferMessage);
                    updateMetrics(transferMessageParser.retrieveMessageId(), TransferStatus.FAIL);
                } else {
                    switch (transferMessageParser.countRoutingNumbersPresent()) {
                        case 1:
                            sendDirectlyToBank(transferMessage, transferMessageParser);
                            updateMetrics(transferMessageParser.retrieveMessageId(), TransferStatus.PROCESSING);
                            break;
                        case 2:
                            processTransactionParty(transferMessage, transferMessageParser);
                            updateMetrics(transferMessageParser.retrieveMessageId(), TransferStatus.PROCESSING);
                            break;
                        default:
                            break;
                    }
                }
            }  catch (JDOMException | IOException e) {
                e.printStackTrace();
                sendToErrorTopic(transferMessage);
            }
        };
    }




//ERROR
    private void sendToErrorTopic(final String transferMessageXML) {
        streamBridge.send(RouterTopicNames.OUTBOUND_TRANSFER_ERROR, "Error routing message - " + transferMessageXML);
    }




    // ONE ROUTING NUMBER PRESENT
    private void sendDirectlyToBank(final String transferMessageXML, TransferMessageParser transferMessageParser) {
        String topic = RouterTopicNames.OUTBOUND_SINGLE_BANK_PREFIX + transferMessageParser.getMatchingRoute();
        Message<String> message = MessageBuilder.withPayload(transferMessageXML).setHeader(KafkaHeaders.MESSAGE_KEY, transferMessageParser.retrieveMessageId().toString().getBytes(StandardCharsets.UTF_8)).build();
        streamBridge.send(topic, message);
    }


    //TWO BANK PREP
    private void processTransactionParty(final String transferMessageXML, final TransferMessageParser transferMessageParser) {
        PaymentParty paymentParty = transferMessageParser.getPaymentParty();
        TransferValidation transferValidation = new TransferValidation();

        transferValidation.setMessageId(paymentParty.getMessageId());
        transferValidation.setTransferMessage(transferMessageXML);
        transferValidation.setDebtorAccount(paymentParty.getDebtorAccount());
        transferValidation.setCreditorAccount(paymentParty.getCreditorAccount());
        transferValidation.setAmount(paymentParty.getAmount());

        streamBridge.send(RouterTopicNames.INBOUND_VALIDATION_TOPIC, transferValidation);
    }




    @Bean
    public Consumer<Message<TransferValidation>> validationConsumer() {
        return message -> {
            TransferValidation validation = message.getPayload();
            switch (validation.getCurrentLeg()) {
                case 1:
                    // Initial state - has not been sent to any banks yet
                    streamBridge.send(VALIDATE_USER + validation.getDebtorAccount().getRoutingNumber(),
                            MessageBuilder
                                    .withPayload(mapper.toJsonString(validation))
                                    .setHeader(KafkaHeaders.MESSAGE_KEY, validation.getMessageId().toString().getBytes())
                                    .build());

                    break;
                case 2:
                    // debtor bank has responded and is content with user data and amount
                    streamBridge.send(VALIDATE_USER + validation.getCreditorAccount().getRoutingNumber(),
                            MessageBuilder.withPayload(mapper.toJsonString(validation))
                                    .setHeader(KafkaHeaders.MESSAGE_KEY, validation.getMessageId().toString().getBytes())
                                    .build());
                    break;
                case 3:
                    // creditor bank has responded and is content with user data
                    streamBridge.send(FUNDS_SINGLE_ACCOUNT + validation.getDebtorAccount().getRoutingNumber(), validation.getTransferMessage());
                    streamBridge.send(FUNDS_SINGLE_ACCOUNT + validation.getCreditorAccount().getRoutingNumber(), validation.getTransferMessage());
                    updateMetrics(validation.getMessageId(), TransferStatus.PERSISTED);
                    break;
                default:
                    // leg is 0. bank could not validate user.
                    sendToErrorTopic(validation.getTransferMessage());
                    updateMetrics(validation.getMessageId(), TransferStatus.FAIL);
            }
        };
    }







    // TRANSFER METRICS

    private void updateMetrics(Long messageId, TransferStatus status) {
        Message<TransferStatus> message = MessageBuilder.withPayload(status)
                .setHeader(KafkaHeaders.MESSAGE_KEY, messageId.toString().getBytes(StandardCharsets.UTF_8))
                .build();
        streamBridge.send(RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC, message);
    }


    public static final String STORE = RouterTopicNames.TRANSFER_STORE;
    @Bean
    public Function<KStream<String, TransferStatus>, KTable<String, String>> upsertMetric() {
        return stream -> stream
                .mapValues(Enum::toString)
                .toTable(Named.as(RouterTopicNames.OUTBOUND_TRANSFER_DATA_TOPIC), Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(STORE).withKeySerde(Serdes.String()).withValueSerde(Serdes.String()));
    }


}



