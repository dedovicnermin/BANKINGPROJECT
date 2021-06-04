package tech.nermindedovic.routerstreams.config.processors;



import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;
import tech.nermindedovic.routerstreams.utils.RouterJsonMapper;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Properties;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessInitialTransferTest {


    @Mock
    RouterJsonMapper mapper;

    @Mock
    TransferMessageParser parser;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, PaymentData> invalidTransferMessageTopic;    // [invalidTransferMessage : 0]
    private TestOutputTopic<String, PaymentData> singleBankTopic;        // [singleBankTransfer : 1]
    private TestOutputTopic<String, PaymentData> doubleBankTopic;           // [doubleBankTransfer: 2]


    Serde<String> stringSerde = Serdes.String();
    Serde<PaymentData> paymentDataSerde = new CustomSerdes.PaymentDataSerde();

    final Properties props = new Properties();


    @BeforeAll
    void setupProps() {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-routerProcessInitialTransfer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, paymentDataSerde.getClass());
    }

    @BeforeEach
    void setup() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> stream = builder.stream(RouterTopicNames.INBOUND_INITIAL_TRANSFER_TOPIC, Consumed.with(stringSerde, stringSerde));
        KStream<String, PaymentData>[] outboundStreams = new TransferFundsProcessor(mapper, parser).processInitialTransfer().apply(stream);

        outboundStreams[0].to(RouterTopicNames.TRANSFER_ERROR_HANDLER_TOPIC, Produced.with(stringSerde, paymentDataSerde));
        outboundStreams[1].to(RouterTopicNames.TRANSFER_SINGLEBANK_PROCESSOR, Produced.with(stringSerde, paymentDataSerde));
        outboundStreams[2].to(RouterTopicNames.TRANSFER_DOUBLEBANK_PROCESSOR, Produced.with(stringSerde, paymentDataSerde));
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        inputTopic = testDriver.createInputTopic(RouterTopicNames.INBOUND_INITIAL_TRANSFER_TOPIC, stringSerde.serializer(), stringSerde.serializer());
        invalidTransferMessageTopic = testDriver.createOutputTopic(RouterTopicNames.TRANSFER_ERROR_HANDLER_TOPIC, stringSerde.deserializer(), paymentDataSerde.deserializer());
        singleBankTopic = testDriver.createOutputTopic(RouterTopicNames.TRANSFER_SINGLEBANK_PROCESSOR, stringSerde.deserializer(), paymentDataSerde.deserializer());
        doubleBankTopic = testDriver.createOutputTopic(RouterTopicNames.TRANSFER_DOUBLEBANK_PROCESSOR, stringSerde.deserializer(), paymentDataSerde.deserializer());

    }

    @AfterEach
    void reset() { testDriver.close(); }


    @Test
    void onBogusTransferMessageXML_willRouteMessageToErrorHandler() {
        String bogusXML = "<BOGUS>";
        PaymentData paymentData = new PaymentData();
        paymentData.setTransferMessageXml(bogusXML);

        when(parser.build(bogusXML)).thenReturn(paymentData);

        inputTopic.pipeInput(bogusXML);
        assertThrows(NoSuchElementException.class, () -> singleBankTopic.readValue());
        assertThrows(NoSuchElementException.class, () -> doubleBankTopic.readValue());
        assertThat(invalidTransferMessageTopic.readValue()).isEqualTo(paymentData);

    }


    @Test
    void onSingleBankTransfer_willRouteToSingleBankProcessor() {
        String transferMsgXMLPretender = "pretendXML";
        long routing = 111;
        PaymentData paymentData = new PaymentData(1L, BigDecimal.ONE, new Debtor(1, routing), new Creditor(2, routing), transferMsgXMLPretender);
        when(parser.build(transferMsgXMLPretender)).thenReturn(paymentData);

        inputTopic.pipeInput(transferMsgXMLPretender);
        assertThrows(NoSuchElementException.class, () -> doubleBankTopic.readValue());
        assertThrows(NoSuchElementException.class, () -> invalidTransferMessageTopic.readValue());
        assertThat(singleBankTopic.readValue()).isEqualTo(paymentData);


        long routing2 = 222;
        PaymentData paymentData2 = new PaymentData(2L, BigDecimal.TEN, new Debtor(1, routing2), new Creditor(2, routing2), transferMsgXMLPretender);
        when(parser.build(transferMsgXMLPretender)).thenReturn(paymentData2);

        inputTopic.pipeInput(transferMsgXMLPretender);
        assertThrows(NoSuchElementException.class, () -> doubleBankTopic.readValue());
        assertThrows(NoSuchElementException.class, () -> invalidTransferMessageTopic.readValue());
        assertThat(singleBankTopic.readValue()).isEqualTo(paymentData2);

    }


    @Test
    void onDoubleBankTransfer_willRouteToDoubleBankProcessor() {
        String transferMsgXMLPretender = "pretendXML";
        PaymentData paymentData = new PaymentData(1L, BigDecimal.ONE, new Debtor(1L, 111L), new Creditor(2L, 222L), transferMsgXMLPretender);
        when(parser.build(transferMsgXMLPretender)).thenReturn(paymentData);

        inputTopic.pipeInput(transferMsgXMLPretender);
        assertThrows(NoSuchElementException.class, () -> singleBankTopic.readValue());
        assertThrows(NoSuchElementException.class, () -> invalidTransferMessageTopic.readValue());
        assertThat(doubleBankTopic.readValue()).isEqualTo(paymentData);
    }



}
