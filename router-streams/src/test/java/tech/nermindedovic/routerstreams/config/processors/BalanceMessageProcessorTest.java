package tech.nermindedovic.routerstreams.config.processors;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.routerstreams.utils.BalanceMessageParser;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

import java.util.NoSuchElementException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BalanceMessageProcessorTest {

    private TopologyTestDriver testDriver;

    private TestInputTopic<String, String> inboundTopic;
    private TestOutputTopic<String, String> route111Topic;
    private TestOutputTopic<String, String> route222Topic;
    private TestOutputTopic<String, String> errorOutboundTopic;

    Serde<String> stringSerde = Serdes.String();

    @Mock
    BalanceMessageParser balanceMessageParser;

    private static final Properties props = new Properties();


    @BeforeAll
    void setup() {
        // dummy props for test driver
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-balanceProcessors");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:773");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass());
    }


    @BeforeEach
    void beforeEach() {
        // create topology that handles balance message processing
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> stream = builder.stream(RouterTopicNames.INBOUND_BALANCE_REQUEST_TOPIC);
        KStream<String, String>[] outboundStreams = new BalanceProcessor(balanceMessageParser).balanceRequestProcessor().apply(stream);
        outboundStreams[0].to(RouterTopicNames.OUTBOUND_BALANCE_REQUEST_PREFIX + "111", Produced.with(stringSerde, stringSerde));
        outboundStreams[1].to(RouterTopicNames.OUTBOUND_BALANCE_REQUEST_PREFIX + "222", Produced.with(stringSerde, stringSerde));
        outboundStreams[2].to(RouterTopicNames.OUTBOUND_BALANCE_RETURN_TOPIC, Produced.with(stringSerde, stringSerde));
        Topology topology = builder.build();


        // create test driver
        testDriver = new TopologyTestDriver(topology, props);


        // define I/O test topics
        inboundTopic = testDriver.createInputTopic(RouterTopicNames.INBOUND_BALANCE_REQUEST_TOPIC, stringSerde.serializer(), stringSerde.serializer());
        route111Topic = testDriver.createOutputTopic(RouterTopicNames.OUTBOUND_BALANCE_REQUEST_PREFIX + "111", stringSerde.deserializer(), stringSerde.deserializer());
        route222Topic = testDriver.createOutputTopic(RouterTopicNames.OUTBOUND_BALANCE_REQUEST_PREFIX + "222", stringSerde.deserializer(), stringSerde.deserializer());
        errorOutboundTopic = testDriver.createOutputTopic(RouterTopicNames.OUTBOUND_BALANCE_RETURN_TOPIC, stringSerde.deserializer(), stringSerde.deserializer());
    }
    
    
    @AfterEach
    void reset() {
        testDriver.close();
    }



    @Test
    void onInvalidXML_orUnrecognizedRoute_routesBackToTransformerTopic() {


        String bad_xml = "BAD XML";
        when(balanceMessageParser.getRoute(bad_xml)).thenReturn("0");

        inboundTopic.pipeInput(bad_xml);
        assertThrows(NoSuchElementException.class, () -> route111Topic.readValue());
        assertThrows(NoSuchElementException.class, () -> route222Topic.readValue());
        assertThat(errorOutboundTopic.readValue()).isNotNull();

    }


    @Test
    void onRequestsWith_111_routesToCorrectRoute() {
        String routeMock = "111";
        when(balanceMessageParser.getRoute(routeMock)).thenReturn(routeMock);

        inboundTopic.pipeInput(routeMock);
        assertThrows(NoSuchElementException.class, () -> route222Topic.readValue());
        assertThrows(NoSuchElementException.class, () -> errorOutboundTopic.readValue());
        assertThat(route111Topic.readValue()).isNotNull();

    }


    @Test
    void onRequestsWith_222_routesToCorrectRoute() {
        String routeMock = "222";
        when(balanceMessageParser.getRoute(routeMock)).thenReturn(routeMock);

        inboundTopic.pipeInput(routeMock);
        assertThrows(NoSuchElementException.class, () -> route111Topic.readValue());
        assertThrows(NoSuchElementException.class, () -> errorOutboundTopic.readValue());
        assertThat(route222Topic.readValue()).isNotNull();

    }









}
