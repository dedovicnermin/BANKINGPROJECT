package tech.nermindedovic.routerstreams.config;





import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.nermindedovic.routerstreams.business.domain.Account;
import tech.nermindedovic.routerstreams.business.domain.TransferStatus;
import tech.nermindedovic.routerstreams.business.domain.TransferValidation;
import tech.nermindedovic.routerstreams.business.service.TransferStatusService;
import tech.nermindedovic.routerstreams.config.TransferFundsProcessor;


import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(properties = {"spring.autoconfigure.exclude="
        + "org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}",
})
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = {"funds.transfer.request", "funds.transfer.error", "funds.transfer.111", "funds.transfer.222", "router.validate.transfer", "funds.validate.111", "funds.validate.222", "funds.transfer.status", "transfer.status"})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferFundsProcessorTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired EmbeddedKafkaBroker embeddedKafkaBroker;


    @AfterAll
    void shutDown () {
        embeddedKafkaBroker.destroy();
    }

    String xmlWithInvalidRoutes = "<TransferMessage>" +
            "<messageId>8785179</messageId>" +
            "<debtor><accountNumber>123</accountNumber><routingNumber>111</routingNumber></debtor>" +
            "<creditor><accountNumber>567</accountNumber>" +
            "<routingNumber>900</routingNumber>" +
            "</creditor>" +
            "<date>12-12-2020</date>" +
            "<amount>10.00</amount>" +
            "<memo>here you go friend</memo>" +
            "</TransferMessage>";
    /**
     * TRANSFORMER XML      ->      consumeInitialTransfer().accept         ->      streamBridge send to error topic
     */
    @Test
    void onIncomingTransferMessageXML_whenContainsUnknownRoutingNumber_willDirectToErrorTopic() throws InterruptedException {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-error", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("funds.transfer.error");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) record -> {
            log.info("TEST CONSUMER :" + record);
            records.add(record);
        });

        container.setBeanName("errorTests");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("funds.transfer.request");
        template.sendDefault(xmlWithInvalidRoutes);
        template.flush();

        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).containsIgnoringCase("Error");

        container.stop();

    }









    String xmlWithMatchingRoutes_111 = "<TransferMessage>" +
            "<messageId>8785179</messageId>" +
            "<debtor><accountNumber>123</accountNumber><routingNumber>111</routingNumber></debtor>" +
            "<creditor><accountNumber>567</accountNumber>" +
            "<routingNumber>111</routingNumber>" +
            "</creditor>" +
            "<date>12-12-2020</date>" +
            "<amount>10.00</amount>" +
            "<memo>here you go friend</memo>" +
            "</TransferMessage>";
    /**
     * TRANSFORMER XML      ->      consumeInitialTransfer().accept         ->      streamBridge route to bank:111
     */
    @Test
    void onIncomingTransferMessageXML_whenContainsMatchingRoutingNumber_willRouteDirectlyToBankWith111() throws InterruptedException {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-initial-direct", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("funds.transfer.111");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) record -> {
            System.out.println("TEST CONSUMER :" + record);
            records.add(record);
        });

        container.setBeanName("directBankTest");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("funds.transfer.request");
        template.sendDefault(xmlWithMatchingRoutes_111);
        template.flush();

        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(xmlWithMatchingRoutes_111);

        container.stop();

    }







    String xmlWithMatchingRoutes_222 = "<TransferMessage>" +
            "<messageId>8785179</messageId>" +
            "<debtor><accountNumber>123</accountNumber><routingNumber>222</routingNumber></debtor>" +
            "<creditor><accountNumber>567</accountNumber>" +
            "<routingNumber>222</routingNumber>" +
            "</creditor>" +
            "<date>12-12-2020</date>" +
            "<amount>10.00</amount>" +
            "<memo>here you go friend</memo>" +
            "</TransferMessage>";
    /**
     * TRANSFORMER XML      ->      consumeInitialTransfer().accept         ->      streamBridge route to bank:222
     */
    @Test
    void onIncomingTransferMessageXML_whenContainsMatchingRoutingNumber_willRouteDirectlyToBankWith222() throws InterruptedException {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-initial-direct-222", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("funds.transfer.222");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) record -> {
            System.out.println("TEST CONSUMER :" + record);
            records.add(record);
        });

        container.setBeanName("directBankTest");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("funds.transfer.request");
        template.sendDefault(xmlWithMatchingRoutes_222);
        template.flush();

        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(xmlWithMatchingRoutes_222);

        container.stop();

    }








    String xmlWithDiffRoutes = "<TransferMessage>" +
            "<messageId>8785179</messageId>" +
            "<debtor><accountNumber>123</accountNumber><routingNumber>111</routingNumber></debtor>" +
            "<creditor><accountNumber>567</accountNumber>" +
            "<routingNumber>222</routingNumber>" +
            "</creditor>" +
            "<date>12-12-2020</date>" +
            "<amount>10.00</amount>" +
            "<memo>here you go friend</memo>" +
            "</TransferMessage>";
    @Test
    void onDifferentRoutes_leg1_willSendToDebtorRouteForValidation() throws InterruptedException {
        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(8785179)
                .amount(new BigDecimal("10.00"))
                .currentLeg(1)
                .transferMessage(xmlWithDiffRoutes)
                .debtorAccount(new Account(123, 111))
                .creditorAccount(new Account(567, 222))
                .build();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-leg1", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("funds.validate.111");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);

        container.setBeanName("leg1ValidationTest");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("funds.transfer.request");
        template.sendDefault(xmlWithDiffRoutes);
        template.flush();


        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(transferValidation.toJsonString());

        container.stop();

    }








    @Test
    void onDifferentRoutes_leg2_willSendToCreditorForValidation() throws InterruptedException {
        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(8785179)
                .amount(new BigDecimal("10.00"))
                .currentLeg(2)
                .transferMessage(xmlWithDiffRoutes)
                .debtorAccount(new Account(123, 111))
                .creditorAccount(new Account(567, 222))
                .build();



        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-leg2", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("funds.validate.222");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);

        container.setBeanName("leg2ValidationTest");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());


        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, TransferValidation> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,TransferValidation> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("router.validate.transfer");
        template.sendDefault(transferValidation);
        template.flush();


        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(transferValidation.toJsonString());

        container.stop();


    }






    @Test
    void onDifferentRoutes_leg3_willFanOutXML_toBothBanks() throws InterruptedException {
        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(8785179)
                .amount(new BigDecimal("10.00"))
                .currentLeg(3)
                .transferMessage(xmlWithDiffRoutes)
                .debtorAccount(new Account(123, 111))
                .creditorAccount(new Account(567, 222))
                .build();



        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-leg3_111", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("funds.transfer.single.111");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);

        container.setBeanName("leg3_111");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());



        Map<String, Object> consumerProps2 = KafkaTestUtils.consumerProps("test-leg3_222", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf2 = new DefaultKafkaConsumerFactory<>(consumerProps2);
        ContainerProperties containerProperties2 = new ContainerProperties("funds.transfer.single.222");
        KafkaMessageListenerContainer<String, String> container2 = new KafkaMessageListenerContainer<>(cf2, containerProperties2);
        final BlockingQueue<ConsumerRecord<String, String>> records2 = new LinkedBlockingQueue<>();
        container2.setupMessageListener((MessageListener<String, String>) records2::add);

        container2.setBeanName("leg3_222");
        container2.start();
        ContainerTestUtils.waitForAssignment(container2, embeddedKafkaBroker.getPartitionsPerTopic());




        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, TransferValidation> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,TransferValidation> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("router.validate.transfer");
        template.sendDefault(transferValidation);
        template.flush();


        ConsumerRecord<String, String> bankA_record = records.poll(5, TimeUnit.SECONDS);
        assertThat(bankA_record).isNotNull();
        assertThat(bankA_record.value()).isEqualTo(xmlWithDiffRoutes);

        ConsumerRecord<String, String> bankB_record = records2.poll(5, TimeUnit.SECONDS);
        assertThat(bankB_record).isNotNull();
        assertThat(bankB_record.value()).isEqualTo(xmlWithDiffRoutes);

        container.stop();
        container2.stop();

    }



    @Autowired
    TransferFundsProcessor transferFundsProcessor;


    @Test
    void test_upsertMetricTopology() {
        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        final Serde<String> stringSerde = Serdes.String();

//        final KStream<String, TransferStatus> stream = streamsBuilder.stream("funds.transfer.status", Consumed.with(stringSerde, new Serdes.WrapperSerde<>(new JsonSerializer<>(), new JsonDeserializer<>(TransferStatus.class))));
        final KStream<String, TransferStatus> stream = streamsBuilder.stream("transfer.status", Consumed.with(stringSerde, new Serdes.WrapperSerde<>(new JsonSerializer<>(), new JsonDeserializer<>(TransferStatus.class))));




        final Function<KStream<String, TransferStatus>, KTable<String, String>> kStreamKTableFunction = transferFundsProcessor.upsertMetric();
        final KTable<String, String> apply = kStreamKTableFunction.apply(stream);

        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "tester");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());

        try (TopologyTestDriver testDriver = new TopologyTestDriver(streamsBuilder.build(), properties)) {
            TestInputTopic<String, TransferStatus> inputTopic = testDriver.createInputTopic("transfer.status", stringSerde.serializer(), new JsonSerializer<>());

            String key1 = String.valueOf(123L);
            String key2 = String.valueOf(456L);
            String key3 = String.valueOf(456L);
            inputTopic.pipeInput(key1, TransferStatus.FAIL);
            inputTopic.pipeInput(key2, TransferStatus.PROCESSING);
            inputTopic.pipeInput(key3, TransferStatus.PERSISTED);

            final KeyValueStore<String, String> keyValueStore = testDriver.getKeyValueStore(TransferFundsProcessor.STORE);

            assertThat(keyValueStore.get(key1)).isEqualTo(TransferStatus.FAIL.name());
            assertThat(keyValueStore.get(key2)).isEqualTo(TransferStatus.PERSISTED.name());

        }


    }



    @Autowired
    TransferStatusService service;

    @Test
    void canQueryForTransferStatus() throws Exception {
        String xml = "<TransferMessage>" +
                "<messageId>1000000</messageId>" +
                "<debtor><accountNumber>123</accountNumber><routingNumber>111</routingNumber></debtor>" +
                "<creditor><accountNumber>567</accountNumber>" +
                "<routingNumber>900</routingNumber>" +
                "</creditor>" +
                "<date>12-12-2020</date>" +
                "<amount>10.00</amount>" +
                "<memo>here you go friend</memo>" +
                "</TransferMessage>";

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("funds.transfer.request");
        template.sendDefault(xml);
        template.flush();

        Thread.sleep(1000);

        assertThat(service.getStatus(String.valueOf(1000000L))).isEqualTo(TransferStatus.FAIL.name());

    }














}
