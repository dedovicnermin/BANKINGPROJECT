package tech.nermindedovic.routerstreams.config.processors;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.config.serdes.CustomSerdes;
import tech.nermindedovic.routerstreams.utils.RouterJsonMapper;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;
import tech.nermindedovic.routerstreams.utils.TransferMessageParser;

import java.math.BigDecimal;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ValidatedProcessorTest {
    @Mock RouterJsonMapper mapper;
    @Mock TransferMessageParser parser;

    private static final String IN      = RouterTopicNames.VALIDATED_FANOUT_TOPIC,
                                OUT111  = RouterTopicNames.OUTBOUND_SINGLE_BANK_PREFIX + "111",
                                OUT222  = RouterTopicNames.OUTBOUND_SINGLE_BANK_PREFIX + "222",
                                OUTSTATUS = RouterTopicNames.TRANSFER_STATUS_SUCCESS_HANDLER;

    private final Properties props = new Properties();
    private final TransferFundsProcessor transferFundsProcessor = new TransferFundsProcessor(mapper, parser);

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, TransferValidation> inputTopic;
    private TestOutputTopic<String, String> outputTopic111;
    private TestOutputTopic<String, String> outputTopic222;
    private TestOutputTopic<String, String> outputTopicStatusPersisted;

    final Serde<String> stringSerde = Serdes.String();
    final Serde<TransferValidation> validationSerde = new CustomSerdes.TransferValidationSerde();

    @BeforeAll
    void configureProps() {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-validatedProcessor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass());
    }

    @BeforeEach
    void setup() {
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, TransferValidation> stream = builder.stream(IN, Consumed.with(stringSerde, validationSerde));
        transferFundsProcessor.validatedProcessor().accept(stream);
        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        inputTopic          = testDriver.createInputTopic(IN, stringSerde.serializer(), validationSerde.serializer());
        outputTopic111      = testDriver.createOutputTopic(OUT111, stringSerde.deserializer(), stringSerde.deserializer());
        outputTopic222      = testDriver.createOutputTopic(OUT222, stringSerde.deserializer(), stringSerde.deserializer());
        outputTopicStatusPersisted = testDriver.createOutputTopic(OUTSTATUS, stringSerde.deserializer(), stringSerde.deserializer());
    }


    @AfterEach
    void close() {
        testDriver.close();
    }



    @Test
    void givenCompletelyValidatedTransferValidation_willFanOutToBanksAndTransferStatusHandler() {
        TransferValidation validation = TransferValidation.builder()
                .messageId(1L)
                .currentLeg(3)
                .creditorAccount(new Creditor(1L, 111L))
                .debtorAccount(new Debtor(2L, 222L))
                .transferMessage("<XML>")
                .amount(BigDecimal.ONE)
                .build();

        inputTopic.pipeInput(validation);
        assertThat(outputTopic111.readValue()).isEqualTo(validation.getTransferMessage());
        assertThat(outputTopic222.readValue()).isEqualTo(validation.getTransferMessage());
        assertThat(outputTopicStatusPersisted.readValue()).isEqualTo(validation.getTransferMessage());

    }


}
