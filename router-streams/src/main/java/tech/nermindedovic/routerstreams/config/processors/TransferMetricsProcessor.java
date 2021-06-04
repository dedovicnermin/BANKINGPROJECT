package tech.nermindedovic.routerstreams.config.processors;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

import java.util.function.Function;

@Configuration
public class TransferMetricsProcessor {

    //  TESTED : 👍🏼

    public static final String STORE = RouterTopicNames.TRANSFER_STATUS_STORE;

    /**
     * IN: router.metrics.handler-failed
     * OUT: funds.transfer.status
     * @return KStream
     */
    @Bean
    public Function<KStream<String, PaymentData>, KStream<String, TransferStatus>> failedTransferMetricHandler() {
        return input -> input.mapValues(val -> TransferStatus.FAIL);
    }


    /**
     * IN: router.
     * @return
     */
    @Bean
    public Function<KStream<String, PaymentData>, KStream<String, TransferStatus>> processingTransferMetricHandler() {
        return input -> input.mapValues(val -> TransferStatus.PROCESSING);
    }

    @Bean
    public Function<KStream<String, String>, KStream<String, TransferStatus>> persistedTransferMetricHandler() {
        return input -> input.mapValues(val -> TransferStatus.PERSISTED);
    }


    //
    @Bean
    public Function<KStream<String, TransferStatus>, KTable<String, String>> upsertMetric() {
        return stream -> stream
                .mapValues(Enum::toString)
                .toTable(Named.as(RouterTopicNames.OUTBOUND_TRANSFER_DATA_TOPIC), Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(STORE).withKeySerde(Serdes.String()).withValueSerde(Serdes.String()));
    }


    //transferXmlStore == store name

    /**
     * IN: transfer.xml.store.register
     * TABLE: transfer.xml.store.table
     * STORE: transfer.xml.store
     * OUT: transfer.xml.store.output
     * @return KTable
     */
    @Bean
    public Function<KStream<String, PaymentData>, KTable<String, String>> storeTransferMessageXml() {
        return stream -> stream
                .selectKey((k,v) -> v.getMessageId().toString())
                .mapValues(PaymentData::getTransferMessageXml)
                .toTable(Named.as(RouterTopicNames.TRANSFER_XML_TABLE_TOPIC), Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(RouterTopicNames.TRANSFER_XML_STORE_NAME).withKeySerde(Serdes.String()).withValueSerde(Serdes.String()));
    }
}
