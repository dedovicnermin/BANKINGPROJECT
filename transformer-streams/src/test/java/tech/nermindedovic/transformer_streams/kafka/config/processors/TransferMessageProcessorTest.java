package tech.nermindedovic.transformer_streams.kafka.config.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.AvroCreditor;
import tech.nermindedovic.AvroDebtor;
import tech.nermindedovic.AvroTransferMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferMessageProcessorTest {

    @Mock
    XmlMapper mapperMock;

    private final XmlMapper xmlMapper = new XmlMapper();
    private static final String SCHEMA_REGISTRY_SCOPE = TransferMessageProcessorTest.class.getName();
    private static final String MOCK_SCHEMA_REGISTRY_URL = "mock://" + SCHEMA_REGISTRY_SCOPE;
    private static final String SCHEMA_PROP = "schema.registry.url";
    private TopologyTestDriver testDriver;

    private TestInputTopic<String, AvroTransferMessage> requestTopic;
    private TestOutputTopic<String, String> responseTopic;

    @BeforeEach
    void beforeEach() {
        //create topology to handle stream of avro transfer messages
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, AvroTransferMessage> stream = builder.stream("funds.transformer.request");
        KStream<String, String> apply = new TransferMessageProcessor(mapperMock).processTransfer().apply(stream);
        apply.to("funds.transfer.request", Produced.with(Serdes.String(), Serdes.String()));
        Topology topology = builder.build();

        // dummy properties needed for test driver
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-processTransfer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(SCHEMA_PROP, MOCK_SCHEMA_REGISTRY_URL);

        //create test driver
        testDriver = new TopologyTestDriver(topology, props);

        //create serdes used for test record key/value
        Serde<String> stringSerde = Serdes.String();
        Serde<AvroTransferMessage> avroTransferMessageSerde = new SpecificAvroSerde<>();

        // configure serdes to use the same mock schema registry url
        Map<String, String> config = new HashMap<>();
        config.put(SCHEMA_PROP, MOCK_SCHEMA_REGISTRY_URL);
        avroTransferMessageSerde.configure(config, false);


        //define the input and output topics to use
        requestTopic = testDriver.createInputTopic("funds.transformer.request", stringSerde.serializer(), avroTransferMessageSerde.serializer());
        responseTopic = testDriver.createOutputTopic("funds.transfer.request", stringSerde.deserializer(), stringSerde.deserializer());

    }


    @AfterEach
    void afterEach() {
        testDriver.close();
        MockSchemaRegistry.dropScope(SCHEMA_REGISTRY_SCOPE);
    }

    @Test
    void givenAvroBalanceMessage_willConvertToXML() throws JsonProcessingException {
        long MSG_ID = 1, CRED_AN = 1, CRED_RN = 111, DEBT_AN = 2, DEBT_RN = 222;
        BigDecimal amount = new BigDecimal("187.90");
        LocalDate date = LocalDate.now();
        String memo = "test memo";
        AvroTransferMessage transferMessage = AvroTransferMessage.newBuilder()
                .setMessageId(MSG_ID)
                .setCreditor(new AvroCreditor(CRED_AN, CRED_RN))
                .setDebtor(new AvroDebtor(DEBT_AN, DEBT_RN))
                .setAmount(amount.toPlainString())
                .setDate(date.toString())
                .setMemo(memo)
                .build();
        TransferMessage toBeXML = new TransferMessage(MSG_ID, new Creditor(CRED_AN, CRED_RN), new Debtor(DEBT_AN, DEBT_RN), date, amount, memo);
        String expected = xmlMapper.writeValueAsString(toBeXML);


        when(mapperMock.writeValueAsString(toBeXML)).thenReturn(expected);

        requestTopic.pipeInput(transferMessage);
        assertThat(responseTopic.readValue()).isEqualTo(expected);
    }

    @Test
    void whenJsonProcessingException_returnsErrorString() throws JsonProcessingException {
        long MSG_ID = 1, CRED_AN = 1, CRED_RN = 111, DEBT_AN = 2, DEBT_RN = 222;
        BigDecimal amount = new BigDecimal("187.90");
        LocalDate date = LocalDate.now();
        String memo = "test memo";
        AvroTransferMessage transferMessage = AvroTransferMessage.newBuilder()
                .setMessageId(MSG_ID)
                .setCreditor(new AvroCreditor(CRED_AN, CRED_RN))
                .setDebtor(new AvroDebtor(DEBT_AN, DEBT_RN))
                .setAmount(amount.toPlainString())
                .setDate(date.toString())
                .setMemo(memo)
                .build();
        TransferMessage toBeXML = new TransferMessage(MSG_ID, new Creditor(CRED_AN, CRED_RN), new Debtor(DEBT_AN, DEBT_RN), date, amount, memo);

        doThrow(JsonProcessingException.class).when(mapperMock).writeValueAsString(toBeXML);
        requestTopic.pipeInput(transferMessage);
        assertThat(responseTopic.readValue()).contains("error!");




    }




}