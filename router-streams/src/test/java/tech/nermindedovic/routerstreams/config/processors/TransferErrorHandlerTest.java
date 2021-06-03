package tech.nermindedovic.routerstreams.config.processors;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.config.TransferFundsProcessor;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;
import tech.nermindedovic.routerstreams.utils.RouterAppUtils;
import tech.nermindedovic.routerstreams.utils.RouterJsonMapper;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;

import java.util.Properties;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class TransferErrorHandlerTest {


    @Mock
    RouterJsonMapper mapper;

    @Mock
    TransferMessageParser parser;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PaymentData> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    Serde<String> stringSerde = Serdes.String();
    Serde<PaymentData> paymentDataSerde = new CustomSerdes.PaymentDataSerde();

    @BeforeEach
    void setup() {
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, PaymentData> stream = builder.stream(RouterTopicNames.TRANSFER_ERROR_HANDLER_TOPIC, Consumed.with(stringSerde, paymentDataSerde));
        final KStream<String, String> outboundStream = new TransferFundsProcessor(mapper, parser).transferErrorHandler().apply(stream);
        outboundStream.to(RouterTopicNames.OUTBOUND_TRANSFER_ERROR, Produced.with(stringSerde, stringSerde));
        Topology topology = builder.build();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-routerProcessInitialTransfer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, paymentDataSerde.getClass());

        testDriver = new TopologyTestDriver(topology, props);

        inputTopic = testDriver.createInputTopic(RouterTopicNames.TRANSFER_ERROR_HANDLER_TOPIC, stringSerde.serializer(), paymentDataSerde.serializer());
        outputTopic = testDriver.createOutputTopic(RouterTopicNames.OUTBOUND_TRANSFER_ERROR, stringSerde.deserializer(), stringSerde.deserializer());

    }

    @AfterEach
    void reset() {testDriver.close();}


    @Test
    void givenPaymentData_willPrependErrorMessage_alongWithTransferMessageXML() {
        PaymentData paymentData = new PaymentData();
        String xml = "<YOU MADE IT>";
        paymentData.setTransferMessageXml(xml);

        inputTopic.pipeInput(paymentData);
        assertThat(outputTopic.readValue()).isEqualTo(RouterAppUtils.TRANSFER_ERROR_PREFIX + xml);

    }
}
