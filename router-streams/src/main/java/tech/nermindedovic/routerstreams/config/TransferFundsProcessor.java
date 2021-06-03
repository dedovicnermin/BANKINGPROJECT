package tech.nermindedovic.routerstreams.config;


import lombok.extern.slf4j.Slf4j;



import org.apache.kafka.streams.kstream.*;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.utils.RouterAppUtils;
import tech.nermindedovic.routerstreams.utils.RouterJsonMapper;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;
import tech.nermindedovic.routerstreams.business.domain.*;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;



import java.nio.charset.StandardCharsets;
import java.util.Set;

import java.util.function.Function;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@Slf4j
public class TransferFundsProcessor {

    public static final String VALIDATE_USER = RouterTopicNames.OUTBOUND_VALIDATION_PREFIX;
    public static final String FUNDS_SINGLE_ACCOUNT = RouterTopicNames.OUTBOUND_FUNDS_SINGLE_ACCOUNT_PREFIX;
    public static final String STORE = RouterTopicNames.TRANSFER_STORE;


    private  final RouterJsonMapper mapper;
    private final TransferMessageParser parser;
    public TransferFundsProcessor( final RouterJsonMapper routerJsonMapper, final TransferMessageParser transferMessageParser) {

        this.mapper = routerJsonMapper;
        this.parser = transferMessageParser;
    }



    /**
     * parse incoming transfer message.
     * Ensure it has information for a payment party object
     * delegate on number of valid routing numbers
     */


    @Bean
    @SuppressWarnings("unchecked")
    public Function<KStream<String, String>, KStream<String, PaymentData>[]> processInitialTransfer() {
        final Predicate<String, PaymentData> invalidTransferMessage = (key, paymentData) -> paymentData.getMessageId() == null || invalidRoutingNumbersPresent(paymentData);
        final Predicate<String, PaymentData> singleBankTransfer = (key, paymentData) -> getRoutingSet(paymentData).size() == 1;
        final Predicate<String, PaymentData> doubleBankTransfer = (key, paymentData) -> getRoutingSet(paymentData).size() == 2;


        return transfer -> transfer
                .mapValues(parser::build)
                .branch(invalidTransferMessage, singleBankTransfer, doubleBankTransfer);
    }


    @Bean
    public Function<KStream<String, PaymentData>, KStream<String, String>> transferErrorHandler() {
        return input -> input.mapValues(paymentData -> RouterAppUtils.TRANSFER_ERROR_PREFIX + paymentData.getTransferMessageXml());
    }


//    @Bean
//    public Function<KStream<String, PaymentData>, KStream<>>





    // ONE ROUTING NUMBER PRESENT
    private void sendDirectlyToBank(final String transferMessageXML, PaymentData paymentData) {
        String topic = RouterTopicNames.OUTBOUND_SINGLE_BANK_PREFIX + getMatchingRoute(paymentData);
        byte[] bytes = paymentData.getMessageId().toString().getBytes(StandardCharsets.UTF_8);
        Message<String> message = MessageBuilder
                .withPayload(transferMessageXML)
                .setHeader(KafkaHeaders.MESSAGE_KEY, bytes)
                .build();
//        streamBridge.send(topic, message);
    }


    //TWO BANK PREP
    private void processTransactionParty(final String transferMessageXML, final PaymentData paymentData) {
        TransferValidation transferValidation = new TransferValidation();

        transferValidation.setMessageId(paymentData.getMessageId());
        transferValidation.setTransferMessage(transferMessageXML);
        transferValidation.setDebtorAccount(paymentData.getDebtorAccount());
        transferValidation.setCreditorAccount(paymentData.getCreditorAccount());
        transferValidation.setAmount(paymentData.getAmount());

//        streamBridge.send(RouterTopicNames.INBOUND_VALIDATION_TOPIC, transferValidation);
    }



//
//    @Bean
//    public Consumer<Message<TransferValidation>> validationConsumer() {
//        return message -> {
//            TransferValidation validation = message.getPayload();
//            switch (validation.getCurrentLeg()) {
//                case 1:
//                    // Initial state - has not been sent to any banks yet
//                    streamBridge.send(VALIDATE_USER + validation.getDebtorAccount().getRoutingNumber(),
//                            MessageBuilder
//                                    .withPayload(mapper.toJsonString(validation))
//                                    .setHeader(KafkaHeaders.MESSAGE_KEY, validation.getMessageId().toString().getBytes())
//                                    .build());
//
//                    break;
//                case 2:
//                    // debtor bank has responded and is content with user data and amount
//                    streamBridge.send(VALIDATE_USER + validation.getCreditorAccount().getRoutingNumber(),
//                            MessageBuilder.withPayload(mapper.toJsonString(validation))
//                                    .setHeader(KafkaHeaders.MESSAGE_KEY, validation.getMessageId().toString().getBytes())
//                                    .build());
//                    break;
//                case 3:
//                    // creditor bank has responded and is content with user data
//                    streamBridge.send(FUNDS_SINGLE_ACCOUNT + validation.getDebtorAccount().getRoutingNumber(), validation.getTransferMessage());
//                    streamBridge.send(FUNDS_SINGLE_ACCOUNT + validation.getCreditorAccount().getRoutingNumber(), validation.getTransferMessage());
//                    updateMetrics(validation.getMessageId(), TransferStatus.PERSISTED);
//                    break;
//                default:
//                    // leg is 0. bank could not validate user.
//                    sendToErrorTopic(validation.getTransferMessage());
//                    updateMetrics(validation.getMessageId(), TransferStatus.FAIL);
//            }
//        };
//    }







    // TRANSFER METRICS

    private void updateMetrics(Long messageId, TransferStatus status) {
        Message<TransferStatus> message = MessageBuilder.withPayload(status)
                .setHeader(KafkaHeaders.MESSAGE_KEY, messageId.toString().getBytes(StandardCharsets.UTF_8))
                .build();
//        streamBridge.send(RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC, message);
    }


//
//    @Bean
//    public Function<KStream<String, TransferStatus>, KTable<String, String>> upsertMetric() {
//        return stream -> stream
//                .mapValues(Enum::toString)
//                .toTable(Named.as(RouterTopicNames.OUTBOUND_TRANSFER_DATA_TOPIC), Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(STORE).withKeySerde(Serdes.String()).withValueSerde(Serdes.String()));
//    }

    private boolean invalidRoutingNumbersPresent(PaymentData paymentData) {
        Debtor debtor = paymentData.getDebtorAccount();
        Creditor creditor = paymentData.getCreditorAccount();
        if (debtor == null || creditor == null) return true;
        return Stream.of(debtor.getRoutingNumber(), creditor.getRoutingNumber()).anyMatch(routing -> (!(routing.equals(111L) || routing.equals(222L))));
    }


    private Set<Long> getRoutingSet(PaymentData paymentData) {
        return Stream.of(paymentData.getDebtorAccount().getRoutingNumber(), paymentData.getCreditorAccount().getRoutingNumber())
                .collect(Collectors.toSet());
    }


    private Long getMatchingRoute(PaymentData paymentData) {
        return paymentData.getDebtorAccount().getRoutingNumber();
    }


}



