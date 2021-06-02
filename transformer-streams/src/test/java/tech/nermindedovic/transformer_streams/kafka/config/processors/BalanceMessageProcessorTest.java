package tech.nermindedovic.transformer_streams.kafka.config.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.AvroBalanceMessage;
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.transformer_streams.kafka.config.serde.json.CustomSerdes;


import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceMessageProcessorTest {


    @Mock
    XmlMapper mapperMock;

    private final XmlMapper mapper = new XmlMapper();
    private static final String SCHEMA_REGISTRY_SCOPE = TransferMessageProcessorTest.class.getName();
    private static final String MOCK_SCHEMA_REGISTRY_URL = "mock://" + SCHEMA_REGISTRY_SCOPE;
    private static final String SCHEMA_PROP = "schema.registry.url";
    private TopologyTestDriver testDriver;

    private TestInputTopic<String, AvroBalanceMessage> leg1Request;
    private TestOutputTopic<String, String> leg1Response;

    private TestInputTopic<String, String> leg2Request;
    private TestOutputTopic<String, BalanceMessage> leg2Response;


    //serdes used for test record key/val
    Serde<String> stringSerde = Serdes.String();
    Serde<AvroBalanceMessage> avroBalanceMessageSerde = new SpecificAvroSerde<>();
    Serde<BalanceMessage> balanceMessageSerde = new CustomSerdes.BalanceMessageSerde();


    @Test
    void processBalanceLeg1() throws JsonProcessingException {
        testDriver = getLeg1TestDriver();
        configureLeg1InputOutput();

        // configure serdes to use the same mock schema registry url
        avroBalanceMessageSerde.configure(Collections.singletonMap(SCHEMA_PROP, MOCK_SCHEMA_REGISTRY_URL), false);

        long AN = 1, RN = 111;
        String balance = "0.00";
        BalanceMessage toBeXml = new BalanceMessage(AN, RN, balance, false);
        AvroBalanceMessage input = new AvroBalanceMessage(AN, RN, balance, false);

        String expectedXML = mapper.writeValueAsString(toBeXml);
        when(mapperMock.writeValueAsString(toBeXml)).thenReturn(expectedXML);

        leg1Request.pipeInput(input);
        assertThat(leg1Response.readValue()).isEqualTo(expectedXML);


        testDriver.close();
        MockSchemaRegistry.dropScope(SCHEMA_REGISTRY_SCOPE);

    }


    @Test
    void processBalanceLeg1_whenJsonProcessingException_willReturnToString() throws JsonProcessingException {
        testDriver = getLeg1TestDriver();
        configureLeg1InputOutput();
        avroBalanceMessageSerde.configure(Collections.singletonMap(SCHEMA_PROP, MOCK_SCHEMA_REGISTRY_URL), false);

        long AN = 1, RN = 111;
        String balance = "0.00";
        BalanceMessage toBeXml = new BalanceMessage(AN, RN, balance, false);
        AvroBalanceMessage input = new AvroBalanceMessage(AN, RN, balance, false);

        doThrow(JsonProcessingException.class).when(mapperMock).writeValueAsString(toBeXml);

        leg1Request.pipeInput(input);
        assertThat(leg1Response.readValue()).isEqualTo(input.toString());

        testDriver.close();

    }



    @Test
    void processBalanceLeg2() throws JsonProcessingException {
        testDriver = getLeg2TestDriver();
        configureLeg2InputOutput();

        long AN = 245635, RN = 222;
        BalanceMessage expected = new BalanceMessage(AN, RN, "177.32", false);
        String input = mapper.writeValueAsString(expected);

        when(mapperMock.readValue(input, BalanceMessage.class)).thenReturn(expected);

        leg2Request.pipeInput(input);
        assertThat(leg2Response.readValue()).isEqualTo(expected);

        testDriver.close();
    }




    @Test
    void processBalanceLeg2_whenJsonProcessingException_willReturnGenericBalanceMessage() throws JsonProcessingException {
        testDriver = getLeg2TestDriver();
        configureLeg2InputOutput();

        long AN = 4532, RN = 111;
        String balance = "-100.00";
        BalanceMessage message = new BalanceMessage(AN, RN, balance, false);
        String input = mapper.writeValueAsString(message);


        doThrow(JsonProcessingException.class).when(mapperMock).readValue(input, BalanceMessage.class);

        BalanceMessage expectedErrorResponse = new BalanceMessage(0,0,"0.00", true);
        leg2Request.pipeInput(input);
        assertThat(leg2Response.readValue()).isEqualTo(expectedErrorResponse);

        testDriver.close();
    }










    private TopologyTestDriver getLeg1TestDriver() {
        // create topology to handle stream of avro balance messages
        StreamsBuilder builderLeg1 = new StreamsBuilder();
        KStream<String, AvroBalanceMessage> leg1Stream = builderLeg1.stream("balance.transformer.request");
        KStream<String, String> applyLeg1 = new BalanceMessageProcessor(mapperMock).processBalanceLeg1().apply(leg1Stream);
        applyLeg1.to("balance.update.request", Produced.with(stringSerde, stringSerde));
        Topology topology = builderLeg1.build();

        // dummy properties needed for test driver
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-processBalanceLeg1");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(SCHEMA_PROP, MOCK_SCHEMA_REGISTRY_URL);

        return new TopologyTestDriver(topology, props);
    }



    private TopologyTestDriver getLeg2TestDriver() {
        // create topology to handle stream of xml messages
        StreamsBuilder builderLeg2 = new StreamsBuilder();
        KStream<String, String> leg2Stream = builderLeg2.stream("balance.update.response", Consumed.with(stringSerde, stringSerde));
        KStream<String, BalanceMessage> applyLeg2 = new BalanceMessageProcessor(mapperMock).processBalanceLeg2().apply(leg2Stream);
        applyLeg2.to("balance.transformer.response", Produced.with(stringSerde, new CustomSerdes.BalanceMessageSerde()));
        Topology topology = builderLeg2.build();

        // dummy properties needed for test driver
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-processBalanceLeg2");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, CustomSerdes.BalanceMessageSerde.class);


        return new TopologyTestDriver(topology, props);
    }



    private void configureLeg1InputOutput() {
        leg1Request = testDriver.createInputTopic("balance.transformer.request", stringSerde.serializer(), avroBalanceMessageSerde.serializer());
        leg1Response = testDriver.createOutputTopic("balance.update.request", stringSerde.deserializer(), stringSerde.deserializer());
    }

    private void configureLeg2InputOutput() {
        leg2Request = testDriver.createInputTopic("balance.update.response", stringSerde.serializer(), stringSerde.serializer());
        leg2Response = testDriver.createOutputTopic("balance.transformer.response", stringSerde.deserializer(), balanceMessageSerde.deserializer());
    }

}