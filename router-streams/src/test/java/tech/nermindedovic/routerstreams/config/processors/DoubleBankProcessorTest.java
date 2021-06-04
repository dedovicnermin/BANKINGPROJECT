package tech.nermindedovic.routerstreams.config.processors;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;
import tech.nermindedovic.routerstreams.utils.RouterJsonMapper;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;

import java.math.BigDecimal;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class DoubleBankProcessorTest {

    @Mock RouterJsonMapper mapper;
    @Mock TransferMessageParser parser;

    private static final String IN = RouterTopicNames.TRANSFER_DOUBLEBANK_PROCESSOR, OUT = RouterTopicNames.INBOUND_VALIDATION_TOPIC, METRIC_OUT = RouterTopicNames.TRANSFER_STATUS_PROCESSING_HANDLER;
    private final Properties props = new Properties();
    private final TransferFundsProcessor transferFundsProcessor = new TransferFundsProcessor(mapper, parser);

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PaymentData> testInputTopic;
    private TestOutputTopic<String, TransferValidation> testOutputTopic;
    private TestOutputTopic<String, PaymentData> testMetricOutput;

    final Serde<String> stringSerde = Serdes.String();
    final Serde<PaymentData> paymentDataSerde = new CustomSerdes.PaymentDataSerde();
    final Serde<TransferValidation> transferValidationSerde = new CustomSerdes.TransferValidationSerde();



    @BeforeAll
    void configureProperties() {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-doubleBankProcessor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass());
    }



    @BeforeEach
    void setupDriver() {

        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, PaymentData> stream = builder.stream(IN, Consumed.with(stringSerde, paymentDataSerde));
        final KStream<String, TransferValidation> apply = transferFundsProcessor.doubleBankProcessor().apply(stream);
        apply.to(OUT, Produced.with(stringSerde, transferValidationSerde));
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        testInputTopic  = testDriver.createInputTopic(IN, stringSerde.serializer(), paymentDataSerde.serializer());
        testOutputTopic = testDriver.createOutputTopic(OUT, stringSerde.deserializer(), transferValidationSerde.deserializer());
        testMetricOutput = testDriver.createOutputTopic(METRIC_OUT, stringSerde.deserializer(), paymentDataSerde.deserializer());

    }



    @AfterEach
    void close() {
        testDriver.close();
    }




    @Test
    void whenPaymentDataSteamsIn_processorWillSendStatusUpdate_andReturnTransferValidationStream() {
        long msgId = 1;
        Creditor creditor = new Creditor(123123L, 111L);
        Debtor debtor = new Debtor(213423L, 222L);
        String transferXML = "MOCK_XML";

        PaymentData data = new PaymentData(msgId, BigDecimal.ONE, debtor, creditor, transferXML);
        TransferValidation transferValidation = new TransferValidation(msgId, BigDecimal.ONE, 1, null, debtor, creditor);

        testInputTopic.pipeInput(data);
        TestRecord<String, PaymentData> actualMetricOutput = testMetricOutput.readRecord();
        TestRecord<String, TransferValidation> actualOutput = testOutputTopic.readRecord();
        assertThat(actualOutput.getKey()).isEqualTo(msgId + "");
        assertThat(actualOutput.getValue()).isEqualTo(transferValidation);
        assertThat(actualMetricOutput.getKey()).isEqualTo(msgId+"");
        assertThat(actualMetricOutput.getValue()).isEqualTo(data);



    }







}
