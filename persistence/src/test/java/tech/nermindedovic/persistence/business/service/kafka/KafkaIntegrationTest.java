package tech.nermindedovic.persistence.business.service.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.nermindedovic.library.pojos.*;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;



import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


import static org.assertj.core.api.Assertions.assertThat;



@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {KafkaIntegrationTest.OUTBOUND_TRANSFER_ERRORS, KafkaIntegrationTest.INBOUND_BALANCE_REQUEST, KafkaIntegrationTest.OUTBOUND_BALANCE_RESPONSE, KafkaIntegrationTest.INBOUND_TRANSFER_SINGLE_USER, KafkaIntegrationTest.INBOUND_TRANSFER_VALIDATION, KafkaIntegrationTest.OUTBOUND_ROUTER_VALIDATION, KafkaIntegrationTest.INBOUND_TRANSFER_REQUEST})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KafkaIntegrationTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired AccountRepository accountRepository;
    @Autowired TransactionRepository transactionRepository;


    private BlockingQueue<ConsumerRecord<String, String>> error_records;
    private KafkaMessageListenerContainer<String, String> error_container;
    private Producer<String, String> producer;

    XmlMapper mapper = new XmlMapper();
    ObjectMapper jsonMapper = new ObjectMapper();




    public static final String OUTBOUND_TRANSFER_ERRORS = "funds.transfer.error";
    public static final String OUTBOUND_ROUTER_VALIDATION = "router.validate.transfer";
    public static final String INBOUND_TRANSFER_REQUEST = "funds.transfer.111";
    public static final String INBOUND_TRANSFER_SINGLE_USER = "funds.transfer.single.111";
    public static final String INBOUND_TRANSFER_VALIDATION = "funds.validate.111";
    public static final String INBOUND_BALANCE_REQUEST = "balance.update.request.111";
    public static final String OUTBOUND_BALANCE_RESPONSE = "balance.update.response";




    private BlockingQueue<ConsumerRecord<String, String>> records;
    private KafkaMessageListenerContainer<String, String> container;


    private BlockingQueue<ConsumerRecord<String, String>> validationRecords;
    private KafkaMessageListenerContainer<String, String> validationContainer;





    @BeforeAll
    void setup() {
        Map<String, Object> consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("test-transfer-request", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(OUTBOUND_TRANSFER_ERRORS);
        error_container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        error_records = new LinkedBlockingQueue<>();
        error_container.setupMessageListener((MessageListener<String, String>) error_records::add);
        error_container.start();
        ContainerTestUtils.waitForAssignment(error_container, embeddedKafkaBroker.getPartitionsPerTopic());


        Map<String, Object> producerConfigs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producer = new DefaultKafkaProducerFactory<>(producerConfigs, new StringSerializer(), new StringSerializer()).createProducer();


        // BALANCE INTERACTION CONFIG

        Map<String, Object> consumerConfigBalance = new HashMap<>(KafkaTestUtils.consumerProps("test-balance-request", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory1 = new DefaultKafkaConsumerFactory<>(consumerConfigBalance, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties1 = new ContainerProperties(OUTBOUND_BALANCE_RESPONSE);
        container = new KafkaMessageListenerContainer<>(consumerFactory1, containerProperties1);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());



        Map<String, Object> validationConsumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("test-transfer-validation", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory2 = new DefaultKafkaConsumerFactory<>(validationConsumerConfig, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties2 = new ContainerProperties(OUTBOUND_ROUTER_VALIDATION);
        validationContainer = new KafkaMessageListenerContainer<>(consumerFactory2, containerProperties2);
        validationRecords = new LinkedBlockingQueue<>();
        validationContainer.setupMessageListener((MessageListener<String, String>) validationRecords::add);
        validationContainer.start();
        ContainerTestUtils.waitForAssignment(validationContainer, embeddedKafkaBroker.getPartitionsPerTopic());


    }


    @AfterAll
    void shutdown() {
        error_container.stop();
        producer.close();
        container.stop();

        validationContainer.stop();
        embeddedKafkaBroker.destroy();
    }


    /**
     * consumer successfully listens, passes xml to be processed/persisted, balances are updated
     */
    @Test
    @Order(1)
    void onValidTransfer_willConsumeAndPersist() throws JsonProcessingException, InterruptedException {
        accountRepository.save(new Account(11,11,"Ben", BigDecimal.TEN));
        accountRepository.save(new Account(22,22,"Ken", BigDecimal.TEN));


        TransferMessage transferMessage = new TransferMessage(100L, new Creditor(11, 111), new Debtor(22,222), LocalDate.now(), BigDecimal.ONE, "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);

        producer.send(new ProducerRecord<>(INBOUND_TRANSFER_REQUEST, Long.toString(100), xml));
        producer.flush();

        //check error consumer is empty
        ConsumerRecord<String, String> potentialError = error_records.poll(10000, TimeUnit.MILLISECONDS);
        assertThat(potentialError).isNull();


        // check transaction was persisted without generating id
        assertThat(transactionRepository.findById(100L)).isNotEmpty();
        // check balances of users to ensure they have been updated



        assertThat(accountRepository.findById(11L).get().getAccountBalance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(accountRepository.findById(22L).get().getAccountBalance()).isEqualTo(new BigDecimal("9.00"));
    }


    /**
     * invalid accounts raise errors, handled - sending to error topic
     */
    @Test
    @Order(2)
    void givenInvalidAccount_sendsToErrorTopic() throws JsonProcessingException, InterruptedException {
        TransferMessage transferMessage = new TransferMessage(100L, new Creditor(100L, 100L), new Debtor(1L,1L), LocalDate.now(), new BigDecimal("1.00"), "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);

        producer.send(new ProducerRecord<>(INBOUND_TRANSFER_REQUEST, xml));
        producer.flush();

        //check error consumer is not empty
        ConsumerRecord<String, String> potentialError = error_records.poll(10000, TimeUnit.MILLISECONDS);
        assertThat(potentialError).isNotNull();
        assertThat(potentialError.value()).isEqualTo("PERSISTENCE --- Both accounts are not users of this bank.");


    }


    /**
     * Can handle deserialization issues
     */
    @Test
    @Order(3)
    void givenUnserializableTransferMessage_willHandleAndSendToErrorTopic() throws InterruptedException {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, Long> myProducer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new LongSerializer()).createProducer();

        myProducer.send(new ProducerRecord<>(INBOUND_TRANSFER_REQUEST, "key",20L));
        myProducer.flush();

        //check error consumer is not empty
        ConsumerRecord<String, String> potentialError = error_records.poll(10000, TimeUnit.MILLISECONDS);
        assertThat(potentialError).isNotNull();
        assertThat(potentialError.value()).containsIgnoringCase("PERSISTENCE ---");

        myProducer.close(Duration.ofMillis(2000));
    }



    @Test
    @Order(4)
    void onIncomingSingleUserFundsTransfer_willPersistTransactionAndUpdateUserBalance() throws JsonProcessingException, InterruptedException {
        accountRepository.save(new Account(779,111,"Ben", BigDecimal.TEN));
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, String> myProducer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer()).createProducer();

        long messageId = 344566;
        LocalDate date = LocalDate.now();
        TransferMessage transferMessage = new TransferMessage(messageId, new Creditor(779, 111), new Debtor(345,222), date, BigDecimal.ONE, "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);




        myProducer.send(new ProducerRecord<>(INBOUND_TRANSFER_SINGLE_USER, xml));
        myProducer.flush();


        Thread.sleep(10000);
        Optional<Transaction> transaction = transactionRepository.findById(messageId);
        Assertions.assertAll(
                () -> assertThat(transaction).isPresent(),
                () -> assertThat(transaction).contains(new Transaction(messageId, 779, 345, new BigDecimal("1.00"), date,  "Here's one dollar")),
                () -> assertThat(accountRepository.findById(779L)).isNotEmpty(),
                () -> assertThat(accountRepository.findById(779L).get().getAccountBalance()).isEqualTo(new BigDecimal("11.00"))
        );


        myProducer.close(Duration.ofMillis(2000));
    }


    @Test
    @Order(5)
    void onIncomingSingleUserFundsTransfer_whenDebtorIsNativeAccount_willSubtractFromDebtorBalance() throws JsonProcessingException, InterruptedException {
        accountRepository.save(new Account(808773,111,"Kai", BigDecimal.TEN));
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, String> myProducer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer()).createProducer();

        long messageId = 8021058394L;
        LocalDate date = LocalDate.now();
        TransferMessage transferMessage = new TransferMessage(messageId, new Creditor(779, 222), new Debtor(808773,111), date, BigDecimal.ONE, "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);




        myProducer.send(new ProducerRecord<>(INBOUND_TRANSFER_SINGLE_USER, xml));
        myProducer.flush();


        Thread.sleep(10000);
        Optional<Transaction> transaction = transactionRepository.findById(messageId);
        Assertions.assertAll(
                () -> assertThat(transaction).isPresent(),
                () -> assertThat(transaction).contains(new Transaction(messageId, 779, 808773, new BigDecimal("1.00"), date,  "Here's one dollar")),
                () -> assertThat(accountRepository.findById(808773L)).isNotEmpty(),
                () -> assertThat(accountRepository.findById(808773L).get().getAccountBalance()).isEqualTo(new BigDecimal("9.00"))
        );


        myProducer.close(Duration.ofMillis(2000));
    }



    @Test
    @Order(6)
    // LEG1 == debtor check
    void onIncomingTransferValidation_consumesAndReplies_whenLegIsOne() throws JsonProcessingException, InterruptedException {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, String> myProducer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer()).createProducer();

        Debtor debtor = new Debtor(555, 111);
        Creditor creditor = new Creditor(444, 222);
        accountRepository.save(new Account(debtor.getAccountNumber(), debtor.getRoutingNumber(), "BOB", BigDecimal.TEN));

        long messageId = 773202;
        LocalDate date = LocalDate.now();
        TransferMessage transferMessage = new TransferMessage(messageId, creditor, debtor, date, BigDecimal.ONE, "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);

        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(messageId)
                .amount(BigDecimal.ONE)
                .currentLeg(1)
                .debtorAccount(debtor)
                .creditorAccount(creditor)
                .transferMessage(xml)
                .build();
        String json = jsonMapper.writeValueAsString(transferValidation);

        myProducer.send(new ProducerRecord<>(INBOUND_TRANSFER_VALIDATION, messageId+"",json));
        myProducer.flush();

        transferValidation.setCurrentLeg(2);
        String expectedJson = jsonMapper.writeValueAsString(transferValidation);
        ConsumerRecord<String, String> record = validationRecords.poll(2, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(expectedJson);

        myProducer.close(Duration.ofMillis(2000));
    }

    @Test
    @Order(7)
    //LEG2 == creditor account check
    void onIncomingTransferValidation_consumesAndReplies_whenLegIsTwo() throws JsonProcessingException, InterruptedException {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, String> myProducer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer()).createProducer();

        Debtor debtor = new Debtor(900, 222);
        Creditor creditor = new Creditor(100, 111);
        accountRepository.save(new Account(creditor.getAccountNumber(), creditor.getRoutingNumber(), "SHARON", BigDecimal.TEN));

        long messageId = 773779;
        LocalDate date = LocalDate.now();
        TransferMessage transferMessage = new TransferMessage(messageId, creditor, debtor, date, BigDecimal.ONE, "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);

        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(messageId)
                .amount(BigDecimal.ONE)
                .currentLeg(2)
                .debtorAccount(debtor)
                .creditorAccount(creditor)
                .transferMessage(xml)
                .build();
        String json = jsonMapper.writeValueAsString(transferValidation);

        myProducer.send(new ProducerRecord<>(INBOUND_TRANSFER_VALIDATION, messageId+"", json));
        myProducer.flush();

        transferValidation.setCurrentLeg(3);

        String expectedJson = jsonMapper.writeValueAsString(transferValidation);
        ConsumerRecord<String, String> record = validationRecords.poll(2, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(expectedJson);

        myProducer.close(Duration.ofMillis(2000));
    }




    @Test
    @Order(8)
    void onIncomingTransferValidation_ifAccountNotPresent_setsLegTo_0() throws JsonProcessingException, InterruptedException {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, String> myProducer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer()).createProducer();

        Debtor debtor = new Debtor(4532234, 111);
        Creditor creditor = new Creditor(3222122, 222);


        long messageId = 773202;
        LocalDate date = LocalDate.now();
        TransferMessage transferMessage = new TransferMessage(messageId, creditor, debtor, date, BigDecimal.ONE, "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);

        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(messageId)
                .amount(BigDecimal.ONE)
                .currentLeg(1)
                .debtorAccount(debtor)
                .creditorAccount(creditor)
                .transferMessage(xml)
                .build();
        String json = jsonMapper.writeValueAsString(transferValidation);

        myProducer.send(new ProducerRecord<>(INBOUND_TRANSFER_VALIDATION, json));
        myProducer.flush();

        transferValidation.setCurrentLeg(0);
        String expectedJson = jsonMapper.writeValueAsString(transferValidation);
        ConsumerRecord<String, String> record = validationRecords.poll(2, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(expectedJson);

        myProducer.close(Duration.ofMillis(2000));
    }



    @Test
    @Order(9)
    void onIncomingTransferValidation_ifLegIsAnythingOtherThan_1_or_2_setsCurrentLegTo0() throws JsonProcessingException, InterruptedException {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, String> myProducer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer()).createProducer();

        Debtor debtor = new Debtor(4532234, 111);
        Creditor creditor = new Creditor(3222122, 222);


        long messageId = 90929;
        LocalDate date = LocalDate.now();
        TransferMessage transferMessage = new TransferMessage(messageId, creditor, debtor, date, BigDecimal.ONE, "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);

        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(messageId)
                .amount(BigDecimal.ONE)
                .currentLeg(3)
                .debtorAccount(debtor)
                .creditorAccount(creditor)
                .transferMessage(xml)
                .build();
        String json = jsonMapper.writeValueAsString(transferValidation);

        myProducer.send(new ProducerRecord<>(INBOUND_TRANSFER_VALIDATION, json));
        myProducer.flush();


        transferValidation.setCurrentLeg(0);
        String expectedJson = jsonMapper.writeValueAsString(transferValidation);
        ConsumerRecord<String, String> record = validationRecords.poll(2, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(expectedJson);

        myProducer.close(Duration.ofMillis(2000));



    }



    @Test
    @Order(10)
    void onIncomingTransferValidation_whenNativeUserIsDebtorWithInsufficientFunds_willSetLegTo_0() throws JsonProcessingException, InterruptedException {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, String> myProducer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer()).createProducer();

        Debtor debtor = new Debtor(4532234, 111);
        Creditor creditor = new Creditor(3222122, 222);

        accountRepository.save(new Account(debtor.getAccountNumber(), debtor.getRoutingNumber(), "YO GOT TI", BigDecimal.ONE));


        long messageId = 80873548;
        LocalDate date = LocalDate.now();
        TransferMessage transferMessage = new TransferMessage(messageId, creditor, debtor, date, BigDecimal.TEN, "I cant make this payment... but Im gonna try anyway");
        String xml = mapper.writeValueAsString(transferMessage);

        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(messageId)
                .amount(BigDecimal.TEN)
                .currentLeg(1)
                .debtorAccount(debtor)
                .creditorAccount(creditor)
                .transferMessage(xml)
                .build();
        String json = jsonMapper.writeValueAsString(transferValidation);

        myProducer.send(new ProducerRecord<>(INBOUND_TRANSFER_VALIDATION, json));
        myProducer.flush();


        transferValidation.setCurrentLeg(0);
        String expectedJson = jsonMapper.writeValueAsString(transferValidation);
        ConsumerRecord<String, String> record = validationRecords.poll(2, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(expectedJson);

        myProducer.close(Duration.ofMillis(2000));
    }






    @Test
    @Order(11)
    void onIncomingTransferValidation_ifDebtorCannotMakePaymentWithCurrentBalance_setsLegTo0() throws JsonProcessingException,  InterruptedException {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, String> myProducer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer()).createProducer();

        Debtor debtor = new Debtor(555, 111);
        Creditor creditor = new Creditor(444, 222);
        accountRepository.save(new Account(debtor.getAccountNumber(), debtor.getRoutingNumber(), "BOB", BigDecimal.ONE));

        long messageId = 773202;
        LocalDate date = LocalDate.now();
        TransferMessage transferMessage = new TransferMessage(messageId, creditor, debtor, date, BigDecimal.ONE, "Here's one dollar");
        String xml = mapper.writeValueAsString(transferMessage);

        TransferValidation transferValidation = TransferValidation.builder()
                .messageId(messageId)
                .amount(BigDecimal.ONE)
                .currentLeg(1)
                .debtorAccount(debtor)
                .creditorAccount(creditor)
                .transferMessage(xml)
                .build();
        String json = jsonMapper.writeValueAsString(transferValidation);

        myProducer.send(new ProducerRecord<>(INBOUND_TRANSFER_VALIDATION, json));
        myProducer.flush();

        transferValidation.setCurrentLeg(0);
        String expectedJson = jsonMapper.writeValueAsString(transferValidation);
        ConsumerRecord<String, String> record = validationRecords.poll(2, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.value()).isEqualTo(expectedJson);

        myProducer.close(Duration.ofMillis(2000));
    }















    @Test
    @Order(12)
    void test_balanceMessages_WillBeConsumedAndProduced() throws JsonProcessingException, InterruptedException {

        accountRepository.save(new Account(11,11,"Ben", BigDecimal.TEN));

        BalanceMessage balanceMessage = new BalanceMessage(11, 11, "", false);
        String balanceMessageXML = mapper.writeValueAsString(balanceMessage);

        ProducerRecord<String, String> record = new ProducerRecord<>(INBOUND_BALANCE_REQUEST, balanceMessageXML);
        record.headers().add(KafkaHeaders.REPLY_TOPIC, OUTBOUND_BALANCE_RESPONSE.getBytes());

        producer.send(record);
        producer.flush();

        ConsumerRecord<String, String> consumed = records.poll(1000, TimeUnit.MILLISECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo(mapper.writeValueAsString(new BalanceMessage(11,11,"10.00", false)));
    }



    @Test
    @Order(13)
    void test_balanceMessages_willReplyWithGenericBalanceMessage_whenAccountNonExistent() throws JsonProcessingException, InterruptedException {
        BalanceMessage balanceMessage = new BalanceMessage(0, 0, "", false);
        String balanceMessageXML = mapper.writeValueAsString(balanceMessage);
        ProducerRecord<String, String> record = new ProducerRecord<>(INBOUND_BALANCE_REQUEST, balanceMessageXML);
        record.headers().add(KafkaHeaders.REPLY_TOPIC, OUTBOUND_BALANCE_RESPONSE.getBytes());

        producer.send(record);
        producer.flush();

        ConsumerRecord<String, String> consumed = records.poll(1000, TimeUnit.MILLISECONDS);
        assertThat(consumed).isNotNull();
        assertThat(consumed.value()).isEqualTo("<BalanceMessage><accountNumber>0</accountNumber><routingNumber>0</routingNumber><balance></balance><errors>true</errors></BalanceMessage>");
    }



}
