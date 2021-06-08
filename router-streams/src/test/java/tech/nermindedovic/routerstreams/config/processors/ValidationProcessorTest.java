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
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;

import java.util.NoSuchElementException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class ValidationProcessorTest {

    @Mock TransferMessageParser parser;

    private static final String IN = RouterTopicNames.INBOUND_VALIDATION_TOPIC,
                                OUT_ERROR = RouterTopicNames.VALIDATION_ERROR_HANDLER_TOPIC,
                                OUT_111   = RouterTopicNames.OUTBOUND_VALIDATION_PREFIX + "111",
                                OUT_222   = RouterTopicNames.OUTBOUND_VALIDATION_PREFIX + "222",
                                OUT_LEG3  = RouterTopicNames.VALIDATED_PREPARE_FANOUT_TOPIC;

    private final Properties props = new Properties();
    private final TransferFundsProcessor transferFundsProcessor = new TransferFundsProcessor(parser);

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, TransferValidation> testInputTopic;
    private TestOutputTopic<String, TransferValidation> testOutputTopic_0;
    private TestOutputTopic<String, TransferValidation> testOutputTopic_111;
    private TestOutputTopic<String, TransferValidation> testOutputTopic_222;
    private TestOutputTopic<String, TransferValidation> testOutputTopic_leg3;


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
        final KStream<String, TransferValidation> stream = builder.stream(IN, Consumed.with(stringSerde, validationSerde));
        final KStream<String, TransferValidation>[] apply = transferFundsProcessor.validationProcessor().apply(stream);
        apply[0].to(OUT_ERROR, Produced.with(stringSerde, validationSerde));
        apply[1].to(OUT_111, Produced.with(stringSerde, validationSerde));
        apply[2].to(OUT_222, Produced.with(stringSerde, validationSerde));
        apply[3].to(OUT_111, Produced.with(stringSerde, validationSerde));
        apply[4].to(OUT_222, Produced.with(stringSerde, validationSerde));
        apply[5].to(OUT_LEG3, Produced.with(stringSerde, validationSerde));
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        testInputTopic = testDriver.createInputTopic(IN, stringSerde.serializer(), validationSerde.serializer());
        testOutputTopic_0 = testDriver.createOutputTopic(OUT_ERROR, stringSerde.deserializer(), validationSerde.deserializer());
        testOutputTopic_111 = testDriver.createOutputTopic(OUT_111, stringSerde.deserializer(), validationSerde.deserializer());
        testOutputTopic_222 = testDriver.createOutputTopic(OUT_222, stringSerde.deserializer(), validationSerde.deserializer());
        testOutputTopic_leg3 = testDriver.createOutputTopic(OUT_LEG3, stringSerde.deserializer(), validationSerde.deserializer());

    }

    @AfterEach
    void close() {
        testDriver.close();
    }


    @Test
    void whenLegIs0_willBranchToErrorHandlerTopic() {
        TransferValidation validation = new TransferValidation();
        validation.setCurrentLeg(0);

        testInputTopic.pipeInput(validation);
        assertThat(testOutputTopic_0.readValue()).isEqualTo(validation);
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_111.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_222.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_leg3.readValue());
    }


    @Test
    void whenLegIs1_andDebtorRoutingIs111_sendsToBank1ValidationTopic() {
        TransferValidation validation = TransferValidation.builder()
                .currentLeg(1)
                .debtorAccount(new Debtor(1L, 111L))
                .creditorAccount(new Creditor(2L, 222L))
                .build();

        testInputTopic.pipeInput(validation);
        assertThat(testOutputTopic_111.readValue()).isEqualTo(validation);
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_0.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_222.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_leg3.readValue());

    }

    @Test
    void whenLegIs1_andDebtorRoutingIs222_sendsToBank2ValidationTopic() {
        TransferValidation validation = TransferValidation.builder()
                .currentLeg(1)
                .debtorAccount(new Debtor(1L, 222L))
                .creditorAccount(new Creditor(2L, 111L))
                .build();

        testInputTopic.pipeInput(validation);
        assertThat(testOutputTopic_222.readValue()).isEqualTo(validation);
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_0.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_111.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_leg3.readValue());
    }




    @Test
    void whenLegIs2_andCreditorRoutingIs111_sendsToBank1ValidationTopic() {
        TransferValidation validation = TransferValidation.builder()
                .currentLeg(2)
                .debtorAccount(new Debtor(1L, 222L))
                .creditorAccount(new Creditor(2L, 111L))
                .build();

        testInputTopic.pipeInput(validation);
        assertThat(testOutputTopic_111.readValue()).isEqualTo(validation);
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_0.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_222.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_leg3.readValue());
    }



    @Test
    void whenLegIs2_andCreditorRoutingIs222_sendsToBank2ValidationTopic() {
        TransferValidation validation = TransferValidation.builder()
                .currentLeg(2)
                .debtorAccount(new Debtor(1L, 111L))
                .creditorAccount(new Creditor(2L, 222L))
                .build();

        testInputTopic.pipeInput(validation);
        assertThat(testOutputTopic_222.readValue()).isEqualTo(validation);
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_0.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_111.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_leg3.readValue());
    }


    @Test
    void whenLegIs3_willSendToEnrichmentTopic() {
        TransferValidation validation = TransferValidation.builder()
                .currentLeg(3)
                .debtorAccount(new Debtor(1L, 111L))
                .creditorAccount(new Creditor(2L, 222L))
                .build();

        testInputTopic.pipeInput(validation);
        assertThat(testOutputTopic_leg3.readValue()).isEqualTo(validation);
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_0.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_111.readValue());
        assertThrows(NoSuchElementException.class, () -> testOutputTopic_222.readValue());

    }


}
