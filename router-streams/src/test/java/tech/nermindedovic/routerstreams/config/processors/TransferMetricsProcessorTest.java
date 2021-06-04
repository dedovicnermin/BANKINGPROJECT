package tech.nermindedovic.routerstreams.config.processors;


import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferMetricsProcessorTest {

//    router.trsf.error.handler === transferE

    Serde<String> stringSerde = Serdes.String();
    Serde<PaymentData> paymentDataSerde = new CustomSerdes.PaymentDataSerde();
    Serde<TransferStatus> transferStatusSerde = new CustomSerdes.TransferStatusSerde();

    private TopologyTestDriver testDriver;

    // transfer status prep handlers
    private TestInputTopic<String, PaymentData> failedMetricHandlerInput;
    private TestInputTopic<String, PaymentData> processingMetricHandlerInput;
    private TestInputTopic<String, String> persistedMetricHandlerInput;
    private TestOutputTopic<String, TransferStatus> metricHandlerOutput;

    //metric handler
    private TestInputTopic<String, TransferStatus> metricHandlerInput;
    private TestOutputTopic<String, String> statusTableOutput;

    // transfer xml store
    private TestInputTopic<String, PaymentData> xmlStoreInput;
    private TestOutputTopic<String, String> xmlStoreOutput;

    Properties props = new Properties();

    final TransferMetricsProcessor transferMetricsProcessor = new TransferMetricsProcessor();


    @BeforeAll
    void setup() {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-transferMetricsProcessor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass());
    }

    @AfterEach
    void closeTestDriver() {
        testDriver.close();
    }

    @Test
    void failedTransferMetricHandler() {
        setUpFailedHandler();
        String key = "1";
        PaymentData data = new PaymentData();

        failedMetricHandlerInput.pipeInput(key, data);
        TestRecord<String, TransferStatus> expected = new TestRecord<>(key, TransferStatus.FAIL);
        TestRecord<String, TransferStatus> actual = metricHandlerOutput.readRecord();
        assertThat(actual.getKey()).isEqualTo(expected.key());
        assertThat(actual.getValue()).isEqualTo(expected.value());
        assertThat(actual.getHeaders()).isEqualTo(expected.getHeaders());
    }



    @Test
    void processingTransferMetricHandler() {
        setUpProcessingHandler();
        String key = "2";
        PaymentData data = new PaymentData();
        TestRecord<String, TransferStatus> expected = new TestRecord<>(key, TransferStatus.PROCESSING);

        processingMetricHandlerInput.pipeInput(key, data);
        TestRecord<String, TransferStatus> actual = metricHandlerOutput.readRecord();
        assertThat(actual.getKey()).isEqualTo(expected.getKey());
        assertThat(actual.getValue()).isEqualTo(expected.getValue());
        assertThat(actual.getHeaders()).isEqualTo(expected.getHeaders());

    }

    @Test
    void persistedTransferMetricHandler() {
        setUpPersistedHandler();
        String key = "3";
        String value = "<XML_TRANSFER_MESSAGE>";

        persistedMetricHandlerInput.pipeInput(key, value);
        TestRecord<String, TransferStatus> expected = new TestRecord<>(key, TransferStatus.PERSISTED);
        TestRecord<String, TransferStatus> actual = metricHandlerOutput.readRecord();
        assertThat(actual.getKey()).isEqualTo(expected.getKey());
        assertThat(actual.getValue()).isEqualTo(expected.getValue());
        assertThat(actual.getHeaders()).isEqualTo(expected.getHeaders());
    }




    @Test
    void upsertMetric() {
        setUpMetricHandler();
        final List<TransferStatus> upsert = Arrays.asList(TransferStatus.FAIL, TransferStatus.PROCESSING, TransferStatus.PROCESSING, TransferStatus.PROCESSING, TransferStatus.PERSISTED);
        final List<KeyValue<String, TransferStatus>> input = new LinkedList<>();
        final String msgId = "101";
        for (final TransferStatus status : upsert) {
            input.add(new KeyValue<>(msgId, status));
        }

        metricHandlerInput.pipeKeyValueList(input);
        assertThat(statusTableOutput.readKeyValuesToMap()).hasSize(1).containsEntry(msgId, TransferStatus.PERSISTED.name());

    }

    @Test
    void storeTransferMessageXML() {
        setUpStoreTransferMessageXML();
        final List<String> keys = Arrays.asList("1", "2", "3", "4", "5");
        final List<String> values = Arrays.asList("<TRANSFER_MSG_XML_1>", "<TRANSFER_MSG_XML_2>", "<TRANSFER_MSG_XML_3>", "<TRANSFER_MSG_XML_4>", "<TRANSFER_MSG_XML_5>");
        final LinkedList<KeyValue<String, PaymentData>> keyValues = new LinkedList<>();
        for (int i = 0; i < keys.size(); i++) {
            PaymentData paymentData = new PaymentData();
            paymentData.setTransferMessageXml(values.get(i));
            paymentData.setMessageId(i+1L);
            keyValues.add(new KeyValue<>(null, paymentData));
        }

        xmlStoreInput.pipeKeyValueList(keyValues);
        assertThat(xmlStoreOutput.readKeyValuesToMap()).hasSize(keys.size())
                .containsEntry(keys.get(0), values.get(0))
                .containsEntry(keys.get(1), values.get(1))
                .containsEntry(keys.get(2), values.get(2))
                .containsEntry(keys.get(3), values.get(3))
                .containsEntry(keys.get(4), values.get(4));

    }






    private void setUpFailedHandler() {
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, PaymentData> stream = builder.stream(RouterTopicNames.TRANSFER_STATUS_FAILED_HANDLER, Consumed.with(stringSerde, paymentDataSerde));
        final KStream<String, TransferStatus> returnedStream  = transferMetricsProcessor.failedTransferMetricHandler().apply(stream);
        returnedStream.to(RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC, Produced.with(stringSerde, transferStatusSerde));
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        failedMetricHandlerInput = testDriver.createInputTopic(RouterTopicNames.TRANSFER_STATUS_FAILED_HANDLER, stringSerde.serializer(), paymentDataSerde.serializer());
        metricHandlerOutput = testDriver.createOutputTopic(RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC, stringSerde.deserializer(), transferStatusSerde.deserializer());
    }







    private void setUpProcessingHandler() {
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, PaymentData> stream = builder.stream(RouterTopicNames.TRANSFER_STATUS_PROCESSING_HANDLER, Consumed.with(stringSerde, paymentDataSerde));
        final KStream<String, TransferStatus> returnedStream = transferMetricsProcessor.processingTransferMetricHandler().apply(stream);
        returnedStream.to(RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC, Produced.with(stringSerde, transferStatusSerde));
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        processingMetricHandlerInput = testDriver.createInputTopic(RouterTopicNames.TRANSFER_STATUS_PROCESSING_HANDLER, stringSerde.serializer(), paymentDataSerde.serializer());
        metricHandlerOutput = testDriver.createOutputTopic(RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC, stringSerde.deserializer(), transferStatusSerde.deserializer());
    }






    private void setUpPersistedHandler() {
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> stream = builder.stream(RouterTopicNames.TRANSFER_STATUS_SUCCESS_HANDLER, Consumed.with(stringSerde, stringSerde));
        final KStream<String, TransferStatus> apply = transferMetricsProcessor.persistedTransferMetricHandler().apply(stream);
        apply.to(RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC, Produced.with(stringSerde, transferStatusSerde));
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        persistedMetricHandlerInput = testDriver.createInputTopic(RouterTopicNames.TRANSFER_STATUS_SUCCESS_HANDLER, stringSerde.serializer(), stringSerde.serializer());
        metricHandlerOutput = testDriver.createOutputTopic(RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC, stringSerde.deserializer(), transferStatusSerde.deserializer());
    }



    private void setUpMetricHandler() {
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, TransferStatus> stream = builder.stream(RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC, Consumed.with(stringSerde, transferStatusSerde));
        KTable<String, String> apply = transferMetricsProcessor.upsertMetric().apply(stream);
        apply.toStream().to(RouterTopicNames.OUTBOUND_TRANSFER_DATA_TOPIC);
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        metricHandlerInput = testDriver.createInputTopic(RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC, stringSerde.serializer(), transferStatusSerde.serializer());
        statusTableOutput = testDriver.createOutputTopic(RouterTopicNames.OUTBOUND_TRANSFER_DATA_TOPIC, stringSerde.deserializer(), stringSerde.deserializer());
    }


    private void setUpStoreTransferMessageXML() {
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, PaymentData> stream = builder.stream(RouterTopicNames.TRANSFER_XML_REGISTER, Consumed.with(stringSerde, paymentDataSerde));
        final KTable<String, String> apply = transferMetricsProcessor.storeTransferMessageXml().apply(stream);
        apply.toStream().to(RouterTopicNames.TRANSFER_XML_TABLE_TOPIC);
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology,props);

        xmlStoreInput   = testDriver.createInputTopic(RouterTopicNames.TRANSFER_XML_REGISTER, stringSerde.serializer(), paymentDataSerde.serializer());
        xmlStoreOutput  = testDriver.createOutputTopic(RouterTopicNames.TRANSFER_XML_TABLE_TOPIC, stringSerde.deserializer(), stringSerde.deserializer());

    }

}