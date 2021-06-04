package tech.nermindedovic.routerstreams.config.processors;


import lombok.extern.slf4j.Slf4j;


import org.apache.kafka.common.serialization.Serdes;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;
import tech.nermindedovic.routerstreams.utils.RouterAppUtils;
import tech.nermindedovic.routerstreams.utils.RouterJsonMapper;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;
import tech.nermindedovic.routerstreams.business.domain.*;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;




import java.util.Set;


import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@Slf4j
public class TransferFundsProcessor {


    
    // == CONSTRUCTOR ==
    private  final RouterJsonMapper mapper;         // will have to remove. requiring rest to accept and send json : TransferValidation
    private final TransferMessageParser parser;
    public TransferFundsProcessor( final RouterJsonMapper routerJsonMapper, final TransferMessageParser transferMessageParser) {
        this.mapper = routerJsonMapper;
        this.parser = transferMessageParser;
    }








    /**
     * parse incoming transfer message.
     * Ensure it has information for a payment party object
     * delegate on number of valid routing numbers
     *
     * IN: funds.transfer.request
     * OUT:
     *    : [0] - router.trsf.error.handler
     *    : [1] - router.trsf.singlebank.processor
     *    : [2] - router.trsf.doublebank.processor
     *
     *
     */
    @Bean
    @SuppressWarnings("unchecked")
    public Function<KStream<String, String>, KStream<String, PaymentData>[]> processInitialTransfer() {
        final Predicate<String, PaymentData> invalidTransferMessage = (key, paymentData) -> paymentData.getMessageId() == null || invalidRoutingNumbersPresent(paymentData);
        final Predicate<String, PaymentData> singleBankTransfer = (key, paymentData) -> getRoutingSet(paymentData).size() == 1;
        final Predicate<String, PaymentData> doubleBankTransfer = (key, paymentData) -> getRoutingSet(paymentData).size() == 2;


        return transfer -> transfer
                .mapValues(parser::build)
                .through(RouterTopicNames.TRANSFER_XML_REGISTER, Produced.with(Serdes.String(), new CustomSerdes.PaymentDataSerde()))
                .branch(invalidTransferMessage, singleBankTransfer, doubleBankTransfer);
    }











    //"${applicationId}-<name>-repartition"

    /**
     * IN: router.trsf.singlebank.processor
     * OUT:
     *    : [0] - funds.transfer.111
     *    : [1] - funds.transfer.222
     *
     * @return KStream<String,String>[] outbound streams
     */
    @Bean
    @SuppressWarnings("unchecked")
    public Function<KStream<String, PaymentData>, KStream<String, String>[]> singleBankProcessor() {
        final Predicate<String, String> route111 = (key, xml) -> key.equals("111");
        final Predicate<String, String> route222 = (key, xml) -> key.equals("222");

        return input -> input
                .selectKey((key, val) -> val.getMessageId().toString())
                .through(RouterTopicNames.TRANSFER_STATUS_PROCESSING_HANDLER, Produced.with(Serdes.String(), new CustomSerdes.PaymentDataSerde()))
                .map((key, value) -> KeyValue.pair(""+value.getDebtorAccount().getRoutingNumber(), value.getTransferMessageXml()))
                .branch(route111, route222)
                ;
    }











    /**
     * IN: router.trsf.doublebank.processor
     * OUT: router.validate.transfer
     *
     * @return KStream<String, TransferValidation> outbound stream
     */
    @Bean
    public Function<KStream<String, PaymentData>, KStream<String, TransferValidation>> doubleBankProcessor() {
        return input -> input
                .selectKey((key, val) -> val.getMessageId().toString())
                .through(RouterTopicNames.TRANSFER_STATUS_PROCESSING_HANDLER, Produced.with(Serdes.String(),  new CustomSerdes.PaymentDataSerde()))
                .mapValues(this::createValidationFromPaymentData)
                ;
    }













    /**
     * IN: router.validate.transfer
     * OUT:
     *    : [0] - router.validation.error.handler   (native)
     *    : [1] - funds.validate.111                (persistence)
     *    : [2] - funds.validate.222                (persistence)
     *    : [3] - funds.validate.111                (persistence)
     *    : [4] - funds.validate.222                (persistence)
     *    : [5] - router.validated.prepare.fanout   (native)
     *
     * @return KStream<String, TransferValidation>[] outbound streams
     */

    @Bean
    @SuppressWarnings("unchecked")
    public Function<KStream<String, TransferValidation>, KStream<String, TransferValidation>[]> validationProcessor() {
        Predicate<String, TransferValidation> errorLeg      = (key, val) -> val.getCurrentLeg() == 0;
        Predicate<String, TransferValidation> firstLeg111   = (key, val) -> val.getCurrentLeg() == 1 && val.getDebtorAccount().getRoutingNumber() == 111L;    //toDebtorBank - 111
        Predicate<String, TransferValidation> firstLeg222   = (key, val) -> val.getCurrentLeg() == 1 && val.getDebtorAccount().getRoutingNumber() == 222L;    //toDebtorBank - 222
        Predicate<String, TransferValidation> secondLeg111  = (key, val) -> val.getCurrentLeg() == 2 && val.getCreditorAccount().getRoutingNumber() == 111L;    //toCreditorBank - 111
        Predicate<String, TransferValidation> secondLeg222  = (key, val) -> val.getCurrentLeg() == 2 && val.getCreditorAccount().getRoutingNumber() == 222L;    //toCreditorBank - 222
        Predicate<String, TransferValidation> thirdLeg      = (key, val) -> val.getCurrentLeg() == 3;
        return input -> input
                .branch(errorLeg, firstLeg111, firstLeg222, secondLeg111, secondLeg222, thirdLeg);
    }






    /**
     * IN: router.validated.prepare.fanout, transfer.xml.store.output
     * OUT: router.validated.fanout
     * @return KStream : String,String
     */

    @Bean
    public BiFunction<KStream<String, TransferValidation>, KTable<String, String>, KStream<String, TransferValidation>> validatedEnrichmentProcessor() {
        return (validatedStream, transferXmlTable ) -> validatedStream
                .selectKey((k, v) -> v.getMessageId().toString())
                .join(transferXmlTable, (transferValidation, s) -> {
                    transferValidation.setTransferMessage(s);
                    return transferValidation;
                });
    }








    /**
     * IN: router.validated.fanout
     *
     *          || -  funds.transfer.single.111
     * OUT: ----
     *          || -  funds.transfer.single.222
     * @return accept
     */

    // TODO : TEST
    @Bean
    public Consumer<KStream<String, TransferValidation>> validatedProcessor() {
        String routing111 = RouterTopicNames.OUTBOUND_SINGLE_BANK_PREFIX + "111";
        String routing222 = RouterTopicNames.OUTBOUND_SINGLE_BANK_PREFIX + "222";
        Predicate<String, TransferValidation> has111Route = (key, val) -> val.getDebtorAccount().getRoutingNumber() == 111L || val.getCreditorAccount().getRoutingNumber() == 111L;
        Predicate<String, TransferValidation> has222Route = (key, val) -> val.getDebtorAccount().getRoutingNumber() == 222L || val.getCreditorAccount().getRoutingNumber() == 222L;

        return input -> {
            input.filter(has111Route).mapValues(TransferValidation::getTransferMessage).to(routing111, RouterAppUtils.producedWithStringSerdes);
            input.filter(has222Route).mapValues(TransferValidation::getTransferMessage).to(routing222, RouterAppUtils.producedWithStringSerdes);
            input.mapValues(TransferValidation::getTransferMessage).to(RouterTopicNames.TRANSFER_STATUS_SUCCESS_HANDLER, RouterAppUtils.producedWithStringSerdes);
        };
    }










    // === HELPER METHODS ===


    /**
     * Two bank preparation.
     * @param paymentData
     * @return
     */
    private TransferValidation createValidationFromPaymentData(PaymentData paymentData) {
        return TransferValidation.builder()
                .messageId(paymentData.getMessageId())
                .currentLeg(1)
                .debtorAccount(paymentData.getDebtorAccount())
                .creditorAccount(paymentData.getCreditorAccount())
                .amount(paymentData.getAmount())
                .build();
    }








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







}



