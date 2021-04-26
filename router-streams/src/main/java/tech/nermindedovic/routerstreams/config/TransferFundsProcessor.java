package tech.nermindedovic.routerstreams.config;

import lombok.extern.slf4j.Slf4j;


import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.*;

import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import tech.nermindedovic.routerstreams.MessageParser;
import tech.nermindedovic.routerstreams.business.domain.*;



import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@Slf4j
public class TransferFundsProcessor {

    public static final String VALIDATE_USER = "funds.validate.";
    public static final String FUNDS_SINGLE_ACCOUNT = "funds.transfer.single.";

    private final StreamBridge streamBridge;

    public TransferFundsProcessor(final StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }



    /**
     * parse incoming transfer message.
     * Ensure it has information for a payment party object
     * delegate on number of valid routing numbers
     */
    @Bean
    public Consumer<String> consumeInitialTransfer() {
        return transferMessage -> {
            MessageParser messageParser = new MessageParser(transferMessage);
            if (messageParser.getPaymentParty().invalidRoutingNumbersPresent()) {
                sendToErrorTopic(transferMessage);
                updateMetrics(messageParser.retrieveMessageId(), TransferStatus.FAIL);
            } else {
                switch (messageParser.countRoutingNumbersPresent()) {
                    case 1:
                        sendDirectlyToBank(transferMessage, messageParser);
                        updateMetrics(messageParser.retrieveMessageId(), TransferStatus.PROCESSING);
                        break;
                    case 2:
                        processTransactionParty(transferMessage, messageParser);
                        updateMetrics(messageParser.retrieveMessageId(), TransferStatus.PROCESSING);
                        break;
                    default:
                        sendToErrorTopic(transferMessage);
                        updateMetrics(messageParser.retrieveMessageId(), TransferStatus.FAIL);
                        break;
                }
            }
        };
    }




//ERROR
    private void sendToErrorTopic(final String transferMessageXML) {
        streamBridge.send("funds.transfer.error", "Error routing message - " + transferMessageXML);
    }




    // ONE ROUTING NUMBER PRESENT
    private void sendDirectlyToBank(final String transferMessageXML, MessageParser messageParser) {
        String topic = "funds.transfer." + messageParser.getMatchingRoute();
        Message<String> message = MessageBuilder.withPayload(transferMessageXML).build();
        streamBridge.send(topic, message);
        log.info("SEND DIRECT MESSAGE : " + message);
    }



    private void processTransactionParty(final String transferMessageXML, final MessageParser messageParser) {
        PaymentParty paymentParty = messageParser.getPaymentParty();
        TransferValidation transferValidation = new TransferValidation();
        transferValidation.setMessageId(messageParser.retrieveMessageId());
        transferValidation.setTransferMessage(transferMessageXML);
        transferValidation.setDebtorAccount(paymentParty.getDebtorAccount());
        transferValidation.setCreditorAccount(paymentParty.getCreditorAccount());
        transferValidation.setAmount(paymentParty.getAmount());

        streamBridge.send("router.validate.transfer", transferValidation);
    }




    @Bean
    public Consumer<TransferValidation> validationConsumer() {
        return transferValidation -> {

            switch (transferValidation.getCurrentLeg()) {
                case 1:
                    // Initial state - has not been sent to any banks yet
                    streamBridge.send(VALIDATE_USER + transferValidation.getDebtorAccount().getRoutingNumber(), MessageBuilder.withPayload(transferValidation.toJsonString()).build());
                    break;
                case 2:
                    // debtor bank has responded and is content with user data and amount
                    streamBridge.send(VALIDATE_USER + transferValidation.getCreditorAccount().getRoutingNumber(), MessageBuilder.withPayload(transferValidation.toJsonString()).build());
                    break;
                case 3:
                    // creditor bank has responded and is content with user data
                    streamBridge.send(FUNDS_SINGLE_ACCOUNT + transferValidation.getDebtorAccount().getRoutingNumber(), transferValidation.getTransferMessage());
                    streamBridge.send(FUNDS_SINGLE_ACCOUNT + transferValidation.getCreditorAccount().getRoutingNumber(), transferValidation.getTransferMessage());
                    updateMetrics(transferValidation.getMessageId(), TransferStatus.PERSISTED);
                    break;
                default:
                    // leg is 0. bank could not validate user.
                    sendToErrorTopic(transferValidation.getTransferMessage());
                    updateMetrics(transferValidation.getMessageId(), TransferStatus.FAIL);
            }
        };
    }







    // TRANSFER METRICS

    private void updateMetrics(Long messageId, TransferStatus status) {
        Message<TransferStatus> message = MessageBuilder.withPayload(status)
                .setHeader(KafkaHeaders.MESSAGE_KEY, messageId.toString().getBytes(StandardCharsets.UTF_8))
                .build();
        streamBridge.send("funds.transfer.status", message);
    }


    public static final String STORE = "transfer.status.store";
    @Bean
    public Function<KStream<String, TransferStatus>, KTable<String, String>> upsertMetric() {
        return stream -> stream
                .mapValues(Enum::toString)
                .toTable(Named.as("transfer.status"), Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(STORE).withKeySerde(Serdes.String()).withValueSerde(Serdes.String()));
    }


}



