package tech.nermindedovic.routerstreams.config.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

import java.util.function.Function;

@Configuration
@Slf4j
public class TransferMetricsProcessor {

    //  TESTED : 👍🏼

    public static final String STORE = RouterTopicNames.TRANSFER_STATUS_STORE;

    /**
     * IN: router.metrics.handler-failed
     * OUT: funds.transfer.status
     * @return KStream
     */
    @Bean
    public Function<KStream<String, String>, KStream<String, TransferStatus>> failedTransferMetricHandler() {
        return input -> input.mapValues(val -> TransferStatus.FAIL);
    }


    /**
     * IN: router.metrics.handler-processing
     * OUT: funds.transfer.status
     * @return KStream
     */
    @Bean
    public Function<KStream<String, PaymentData>, KStream<String, TransferStatus>> processingTransferMetricHandler() {
        return input -> input
                .mapValues(val -> TransferStatus.PROCESSING);
    }



    /**
     * IN  : router.metrics.handler-persist
     * OUT : funds.transfer.status
     * @return KStream
     */
    @Bean
    public Function<KStream<String, String>, KStream<String, TransferStatus>> persistedTransferMetricHandler() {
        return input -> input.mapValues(val -> TransferStatus.PERSISTED);
    }


    /**
     * IN  : funds.transfer.status
     * OUT : transfer.status
     * @return KTable
     */
    @Bean
    public Function<KStream<String, TransferStatus>, KTable<String, String>> upsertMetric() {
        return stream -> stream
                .mapValues(Enum::toString)
                .toTable(Named.as(RouterTopicNames.OUTBOUND_TRANSFER_DATA_TOPIC), Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(STORE).withKeySerde(Serdes.String()).withValueSerde(Serdes.String()));
    }


    //transferXmlStore == store name

    /**
     * IN: transfer.xml.register
     * OUT: transfer.xml.output
     * @return KStream
     */
    @Bean
    public Function<KStream<String, PaymentData>, KStream<String, String>> storeTransferMessageXml() {
        return input -> input.mapValues(PaymentData::getTransferMessageXml);
    }




}
