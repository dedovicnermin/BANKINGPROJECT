package tech.nermindedovic.routerstreams.config.processors;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;
import tech.nermindedovic.routerstreams.utils.RouterAppUtils;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

import java.util.Properties;
import static org.assertj.core.api.Assertions.assertThat;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferErrorHandlerTest {


    private TopologyTestDriver testDriver;
    private TestInputTopic<String, PaymentData> transferErrorInput;
    private TestInputTopic<String, TransferValidation> validationErrorInput;
    private TestOutputTopic<String, String> transferErrorOutput;

    private TestOutputTopic<String, String> metricsTopic;

    Serde<String> stringSerde = Serdes.String();
    Serde<PaymentData> paymentDataSerde = new CustomSerdes.PaymentDataSerde();
    Serde<TransferValidation> transferValidationSerde = new CustomSerdes.TransferValidationSerde();

    final Properties props = new Properties();


    @BeforeAll
    void setup() {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-errorProcessors");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass());
    }

    @AfterEach
    void closeTestDriver() {
        testDriver.close();
    }



    @Test
    void givenPaymentData_willPrependErrorMessage_alongWithTransferMessageXML() {
        setTestDriverPaymentData(getPaymentDataTopology());

        PaymentData paymentData = new PaymentData();
        String xml = "<YOU MADE IT>";
        paymentData.setTransferMessageXml(xml);
        paymentData.setMessageId(1L);

        transferErrorInput.pipeInput(paymentData);
        assertThat(transferErrorOutput.readValue()).isEqualTo(RouterAppUtils.TRANSFER_ERROR_PREFIX + xml);
        assertThat(metricsTopic.readValue()).isEqualTo(RouterAppUtils.TRANSFER_ERROR_PREFIX + xml);


    }



    @Test
    void givenTransferValidation_willPrependErrorMessage_alongWithTransferMessageXML() {
        setTestDriverTransferValidation(getTransferValidationTopology());

        TransferValidation transferValidation = new TransferValidation();
        String xml = "<TV - YOU MADE IT>";
        transferValidation.setTransferMessage(xml);
        String expected = RouterAppUtils.VALIDATION_ERROR_PREFIX + xml;

        validationErrorInput.pipeInput(transferValidation);
        assertThat(transferErrorOutput.readValue()).isEqualTo(expected);
        assertThat(metricsTopic.readValue()).isEqualTo(expected);

    }



    private Topology getPaymentDataTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, PaymentData> stream = builder.stream(RouterTopicNames.TRANSFER_ERROR_HANDLER_TOPIC, Consumed.with(stringSerde, paymentDataSerde));
        final KStream<String, String> apply = new ErrorProcessor().transferErrorHandler().apply(stream);
        apply.to(RouterTopicNames.OUTBOUND_TRANSFER_ERROR, Produced.with(stringSerde, stringSerde));
        return builder.build();
    }

    private void setTestDriverPaymentData(Topology topology) {
        testDriver = new TopologyTestDriver(topology, props);

        transferErrorInput = testDriver.createInputTopic(RouterTopicNames.TRANSFER_ERROR_HANDLER_TOPIC, stringSerde.serializer(), paymentDataSerde.serializer());
        transferErrorOutput = testDriver.createOutputTopic(RouterTopicNames.OUTBOUND_TRANSFER_ERROR, stringSerde.deserializer(), stringSerde.deserializer());
        metricsTopic = testDriver.createOutputTopic(RouterTopicNames.TRANSFER_STATUS_FAILED_HANDLER, stringSerde.deserializer(), stringSerde.deserializer());
    }



    private Topology getTransferValidationTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, TransferValidation> stream = builder.stream(RouterTopicNames.VALIDATION_ERROR_HANDLER_TOPIC, Consumed.with(stringSerde, transferValidationSerde));
        KStream<String, String> outputStream = new ErrorProcessor().validationErrorHandler().apply(stream);
        outputStream.to(RouterTopicNames.OUTBOUND_TRANSFER_ERROR, Produced.with(stringSerde, stringSerde));
        return builder.build();
    }


    private void setTestDriverTransferValidation(Topology topology) {
        testDriver = new TopologyTestDriver(topology, props);

        validationErrorInput = testDriver.createInputTopic(RouterTopicNames.VALIDATION_ERROR_HANDLER_TOPIC, stringSerde.serializer(), transferValidationSerde.serializer());
        transferErrorOutput = testDriver.createOutputTopic(RouterTopicNames.OUTBOUND_TRANSFER_ERROR, stringSerde.deserializer(), stringSerde.deserializer());
        metricsTopic = testDriver.createOutputTopic(RouterTopicNames.TRANSFER_STATUS_FAILED_HANDLER, stringSerde.deserializer(), stringSerde.deserializer());
    }




}
