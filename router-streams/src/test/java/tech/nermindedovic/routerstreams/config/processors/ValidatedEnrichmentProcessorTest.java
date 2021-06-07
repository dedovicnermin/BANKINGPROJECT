package tech.nermindedovic.routerstreams.config.processors;


import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;
import tech.nermindedovic.routerstreams.utils.RouterJsonMapper;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class ValidatedEnrichmentProcessorTest {

    @Mock RouterJsonMapper mapper;
    @Mock TransferMessageParser parser;

    private static final String IN1 = RouterTopicNames.VALIDATED_PREPARE_FANOUT_TOPIC,
                                IN2 = RouterTopicNames.TRANSFER_XML_STORE_OUTPUT,
                                OUT = RouterTopicNames.VALIDATED_FANOUT_TOPIC;

    private final Properties props = new Properties();
    private final TransferFundsProcessor transferFundsProcessor = new TransferFundsProcessor(mapper, parser);

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, TransferValidation> inputTopic;
    private TestInputTopic<String, String> inputStoreTopic;
    private TestOutputTopic<String, TransferValidation> outputTopic;

    final Serde<String> stringSerde = Serdes.String();
    final Serde<TransferValidation> validationSerde = new CustomSerdes.TransferValidationSerde();


    @BeforeAll
    void configureProps() {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-validationProcessor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass());
    }


    @BeforeEach
    void setup() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, TransferValidation> stream1 = builder.stream(IN1, Consumed.with(stringSerde, validationSerde));
        KTable<String, String> stream2 = builder.table(IN2, Consumed.with(stringSerde, stringSerde));
        KStream<String, TransferValidation> apply = transferFundsProcessor.validatedEnrichmentProcessor().apply(stream1, stream2);
        apply.to(OUT, Produced.with(stringSerde, validationSerde));
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        inputTopic      = testDriver.createInputTopic(IN1, stringSerde.serializer(), validationSerde.serializer());
        inputStoreTopic = testDriver.createInputTopic(IN2, stringSerde.serializer(), stringSerde.serializer());
        outputTopic     = testDriver.createOutputTopic(OUT, stringSerde.deserializer(), validationSerde.deserializer());

    }

    @AfterEach
    void close() {
        testDriver.close();
    }

    @Test
    void givenTransferValidation_joinsForTransferXML_andReturnsStreamWithXMLValue() {

        Long msgId = 100L;
        String transferXML = "<XML>";
        TransferValidation validation = TransferValidation.builder()
                .currentLeg(3)
                .messageId(msgId)
                .build();

        //XML gets stored before this processor is called.
        inputStoreTopic.pipeInput(msgId.toString(), transferXML);
        inputTopic.pipeInput(validation);
        validation.setTransferMessage(transferXML);
        assertThat(outputTopic.readValue()).isEqualTo(validation);
    }



}
