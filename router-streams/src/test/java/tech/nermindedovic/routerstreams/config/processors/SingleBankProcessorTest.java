package tech.nermindedovic.routerstreams.config.processors;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleBankProcessorTest {



    @Mock
    TransferMessageParser parser;



    private static final String IN         = RouterTopicNames.TRANSFER_SINGLEBANK_PROCESSOR;
    private static final String OUT_111    = RouterTopicNames.OUTBOUND_SINGLE_BANK_PREFIX + "111";
    private static final String OUT_222    = RouterTopicNames.OUTBOUND_SINGLE_BANK_PREFIX + "222";
    private static final String OUT_METRIC = RouterTopicNames.INBOUND_TRANSFER_DATA_TOPIC;

    private final Properties props = new Properties();
    private final TransferFundsProcessor transferFundsProcessor = new TransferFundsProcessor(parser);
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PaymentData> testInputTopic;
    private TestOutputTopic<String, String> testOutputTopic111;
    private TestOutputTopic<String, String> testOutputTopic222;
    private TestOutputTopic<String, TransferStatus> testStatusMetricOutput;

    Serde<PaymentData> paymentDataSerde = new CustomSerdes.PaymentDataSerde();
    Serde<String> stringSerde = Serdes.String();
    Serde<TransferStatus> statusSerde = new CustomSerdes.TransferStatusSerde();



    @BeforeAll
    void setupProps() {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-singleBankProcessor()");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, paymentDataSerde.getClass());
    }

    @BeforeEach
    void setupDriver() {
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, PaymentData> stream = builder.stream(IN, Consumed.with(stringSerde, paymentDataSerde));
        final KStream<String, String>[] kStreams = transferFundsProcessor.singleBankProcessor().apply(stream);
        kStreams[0].to(OUT_111);
        kStreams[1].to(OUT_222);
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        testInputTopic = testDriver.createInputTopic(IN, stringSerde.serializer(), paymentDataSerde.serializer());
        testOutputTopic111 = testDriver.createOutputTopic(OUT_111, stringSerde.deserializer(), stringSerde.deserializer());
        testOutputTopic222 = testDriver.createOutputTopic(OUT_222, stringSerde.deserializer(), stringSerde.deserializer());
        testStatusMetricOutput = testDriver.createOutputTopic(OUT_METRIC, stringSerde.deserializer(), statusSerde.deserializer());

    }


    @AfterEach
    void closeTestDriver() {
        testDriver.close();
    }

    // processInitialTransfer will have already assigned a key, hence why we pass one in on input.

    @Test
    void givenPaymentData_containingOnlyRoute111_willRouteToBank111() {

        String expectedMetricKey = "1";
        String transferXML = "<TransferMessage><messageId>1232213</messageId><debtor><accountNumber>1</accountNumber><routingNumber>111</routingNumber></debtor><creditor><accountNumber>3</accountNumber><routingNumber>111</routingNumber></creditor><date>12-12-2020</date><amount>19.29</amount><memo>test memo</memo>";
        PaymentData paymentData = new PaymentData(1L, BigDecimal.TEN, new Debtor(213414L, 111L), new Creditor(23452L, 111L), transferXML);

        testInputTopic.pipeInput(expectedMetricKey,paymentData);
        TestRecord<String, TransferStatus> actualMetricOutput = testStatusMetricOutput.readRecord();

        assertThat(testOutputTopic111.readValue()).contains(transferXML);
        assertThat(actualMetricOutput.getKey()).isEqualTo(expectedMetricKey);
        assertThat(actualMetricOutput.getValue()).isEqualTo(TransferStatus.PROCESSING);
        assertThrows(NoSuchElementException.class, () -> testOutputTopic222.readValue());

    }

    @Test
    void givenPaymentData_containingOnlyRoute222_willRouteToBank222() {
        String expectedMetricKey = "2";
        String transferXML = "<TransferMessage><messageId>3453</messageId><debtor><accountNumber>2231</accountNumber><routingNumber>222</routingNumber></debtor><creditor><accountNumber>45</accountNumber><routingNumber>222</routingNumber></creditor><date>12-12-2020</date><amount>19.29</amount><memo>test memo</memo>";
        PaymentData paymentData = new PaymentData(2L, BigDecimal.TEN, new Debtor(213414L, 222L), new Creditor(23452L, 222L), transferXML);

        testInputTopic.pipeInput(expectedMetricKey ,paymentData);
        TestRecord<String, TransferStatus> actualMetricOutput = testStatusMetricOutput.readRecord();

        assertThat(testOutputTopic222.readValue()).contains(transferXML);
        assertThat(actualMetricOutput.getKey()).isEqualTo(expectedMetricKey);
        assertThat(actualMetricOutput.getValue()).isEqualTo(TransferStatus.PROCESSING);
        assertThrows(NoSuchElementException.class, () -> testOutputTopic111.readValue());
    }





}
