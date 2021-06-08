package tech.nermindedovic.routerstreams;







import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import org.springframework.test.context.ActiveProfiles;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.utils.RouterJsonMapper;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;


import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@ActiveProfiles("integration")
@SpringBootTest(classes = RouterStreamsApplication.class)
@EmbeddedKafka(partitions = 1, brokerProperties = { "request.timeout.ms=1000", "max.poll.interval.ms=5000", "reconnect.backoff.ms=10000", "delivery.timeout.ms=10000", "max.poll.records=1"}, controlledShutdown = true, zkConnectionTimeout = 30, zkSessionTimeout = 30)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(MockitoExtension.class)
class RouterStreamsIntegrationTest {

    @Autowired
    ApplicationContext applicationContext;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired EmbeddedKafkaBroker embeddedKafkaBroker;

    @SpyBean
    RouterJsonMapper mapper;

    String xmlWith1InvalidRoute = "<TransferMessage>" +
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
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(RouterTopicNames.OUTBOUND_TRANSFER_ERROR);
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
        template.setDefaultTopic(RouterTopicNames.INBOUND_INITIAL_TRANSFER_TOPIC);
        template.sendDefault(xmlWith1InvalidRoute);
        template.flush();

        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).containsIgnoringCase("Error");

        container.stop();
        records.clear();

        container.stop(false);


    }


    @Test
    void onIncomingTransferMessageXML_whenContainsNoValidRoutingNumbers_willDirectToErrorTopic() throws InterruptedException {
        String xmlWithInvalidRoutes = "<TransferMessage>" +
                "<messageId>909833</messageId>" +
                "<debtor><accountNumber>123</accountNumber><routingNumber>356</routingNumber></debtor>" +
                "<creditor><accountNumber>567</accountNumber>" +
                "<routingNumber>900</routingNumber>" +
                "</creditor>" +
                "<date>12-12-2020</date>" +
                "<amount>10.00</amount>" +
                "<memo>here you go friend</memo>" +
                "</TransferMessage>";

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-error-noValidRouting", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(RouterTopicNames.OUTBOUND_TRANSFER_ERROR);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);

        container.setBeanName("errorTests-noValidRouting");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        records.clear();
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(RouterTopicNames.INBOUND_INITIAL_TRANSFER_TOPIC);
        template.sendDefault(xmlWithInvalidRoutes);
        template.flush();


        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).containsSequence("ERROR");

        container.stop(false);
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
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties("funds.transfer.111");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);

        container.setBeanName("directBankTest");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(RouterTopicNames.INBOUND_INITIAL_TRANSFER_TOPIC);
        template.sendDefault(xmlWithMatchingRoutes_111);
//        template.flush();

        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(xmlWithMatchingRoutes_111);

        container.stop(false);

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
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());
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
        template.setDefaultTopic(RouterTopicNames.INBOUND_INITIAL_TRANSFER_TOPIC);
        template.sendDefault(xmlWithMatchingRoutes_222);
        template.flush();

        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(xmlWithMatchingRoutes_222);

        container.stop(false);

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
                .messageId(8785179L)
                .amount(new BigDecimal("10.00"))
                .currentLeg(1)
                .transferMessage(null)
                .debtorAccount(new Debtor(123, 111))
                .creditorAccount(new Creditor(567, 222))
                .build();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-leg1", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());
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
        template.setDefaultTopic(RouterTopicNames.INBOUND_INITIAL_TRANSFER_TOPIC);
        template.sendDefault(xmlWithDiffRoutes);
        template.flush();


        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(mapper.toJsonString(transferValidation));

        container.stop(false);

    }








    @Test
    void onDifferentRoutes_leg2_willSendToCreditorForValidation() throws InterruptedException {
        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(8785179L)
                .amount(new BigDecimal("10.00"))
                .currentLeg(2)
                .transferMessage(xmlWithDiffRoutes)
                .debtorAccount(new Debtor(123, 111))
                .creditorAccount(new Creditor(567, 222))
                .build();



        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-leg2", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());
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
        template.setDefaultTopic(RouterTopicNames.INBOUND_VALIDATION_TOPIC);
        template.sendDefault(transferValidation);
        template.flush();


        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(mapper.toJsonString(transferValidation));

        container.stop(false);


    }






    @Test
    void onDifferentRoutes_leg3_willFanOutXML_toBothBanks() throws InterruptedException {
        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(8785179L)
                .amount(new BigDecimal("10.00"))
                .currentLeg(3)
                .transferMessage(xmlWithDiffRoutes)
                .debtorAccount(new Debtor(123, 111))
                .creditorAccount(new Creditor(567, 222))
                .build();



        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-leg3_111", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("funds.transfer.single.111");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);

        container.setBeanName("leg3_111");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());



        Map<String, Object> consumerProps2 = KafkaTestUtils.consumerProps("test-leg3_222", "false", embeddedKafkaBroker);
        consumerProps2.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps2.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        DefaultKafkaConsumerFactory<String, String> cf2 = new DefaultKafkaConsumerFactory<>(consumerProps2);
        ContainerProperties containerProperties2 = new ContainerProperties("funds.transfer.single.222");
        KafkaMessageListenerContainer<String, String> container2 = new KafkaMessageListenerContainer<>(cf2, containerProperties2);
        final BlockingQueue<ConsumerRecord<String, String>> records2 = new LinkedBlockingQueue<>();
        container2.setupMessageListener((MessageListener<String, String>) records2::add);

        container2.setBeanName("leg3_222");
        container2.start();
        ContainerTestUtils.waitForAssignment(container2, embeddedKafkaBroker.getPartitionsPerTopic());





        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, TransferValidation> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,TransferValidation> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(RouterTopicNames.VALIDATED_FANOUT_TOPIC);
//        template.setDefaultTopic("router.validate.transfer");
        template.sendDefault(transferValidation.getMessageId().toString(),transferValidation);
        template.flush();



        ConsumerRecord<String, String> bankA_record = records.poll(10, TimeUnit.SECONDS);
        assertThat(bankA_record).isNotNull();
        assertThat(bankA_record.value()).isEqualTo(xmlWithDiffRoutes);

        ConsumerRecord<String, String> bankB_record = records2.poll(10, TimeUnit.SECONDS);
        assertThat(bankB_record).isNotNull();
        assertThat(bankB_record.value()).isEqualTo(xmlWithDiffRoutes);

        container.stop(false);
        container2.stop(false);

    }



    @Test
    void onDifferentRoutes_willSendToCreditorForValidation() throws InterruptedException {
        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(8785179L)
                .amount(new BigDecimal("10.00"))
                .currentLeg(0)
                .transferMessage(xmlWithDiffRoutes)
                .debtorAccount(new Debtor(123, 111))
                .creditorAccount(new Creditor(567, 222))
                .build();



        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-errorLeg", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(RouterTopicNames.OUTBOUND_TRANSFER_ERROR);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);

        container.setBeanName("errorLeg");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());


        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, TransferValidation> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,TransferValidation> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(RouterTopicNames.INBOUND_VALIDATION_TOPIC);
        template.sendDefault(transferValidation);
        template.flush();


        ConsumerRecord<String, String> consumed = records.poll(5, TimeUnit.SECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).contains("ERROR");

        container.stop(false);


    }




    // BALANCE REQUEST


    final String balanceMessageXML_111 = "<BalanceMessage><accountNumber>200040</accountNumber><routingNumber>111</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";
    final String balanceMessageXML_222 = "<BalanceMessage><accountNumber>200040</accountNumber><routingNumber>222</routingNumber><balance></balance><errors>false</errors></BalanceMessage>";


    @Test
    void onBalanceMessagesWith_111_routesToCorrectBank() throws InterruptedException {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-balance-111", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("balance.update.request.111");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) record -> {
            log.info("TEST CONSUMER :" + record);
            records.add(record);
        });

        container.setBeanName("balanceTest-111");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("balance.update.request");
        template.sendDefault(balanceMessageXML_111);
        template.flush();


        ConsumerRecord<String, String> record = records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(balanceMessageXML_111);

        container.stop(false);

    }


    @Test
    void onBalanceMessagesWith_222_routesToCorrectBank() throws InterruptedException {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-balance-222", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("balance.update.request.222");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) record -> {
            log.info("TEST CONSUMER :" + record);
            records.add(record);
        });

        container.setBeanName("balanceTest-222");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String,String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic("balance.update.request");
        template.sendDefault(balanceMessageXML_222);
        template.flush();


        ConsumerRecord<String, String> record = records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(balanceMessageXML_222);

        container.stop(false);

    }







    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void contextLoads()  {
        Assertions.assertTimeout(Duration.ofSeconds(10),() -> RouterStreamsApplication.main(new String[]{}));
    }


}
