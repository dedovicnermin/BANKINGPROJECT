package tech.nermindedovic.rest.api.elastic;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@ActiveProfiles("integration")
@Testcontainers
@Slf4j
class ElasticIntegrationTest {

    // ELASTIC TEST CONFIG
    private static final String ELASTIC_VERSION = "7.9.3";
    @Container
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch-oss" + ":" + ELASTIC_VERSION);
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("elastic-endpoint", elasticsearchContainer::getHttpHostAddress);
    }
    @Autowired
    ElasticsearchRestTemplate template;

    @Autowired
    ElasticAPI api;


    @BeforeAll
    static void setupElastic() {
        elasticsearchContainer.start();
        elasticsearchContainer.waitingFor(new WaitAllStrategy(WaitAllStrategy.Mode.WITH_MAXIMUM_OUTER_TIMEOUT));
    }


    @AfterAll
    static void destroy() {
        elasticsearchContainer.stop();
    }


    @BeforeEach
    void reset() {
        assertThat(elasticsearchContainer.isRunning()).isTrue();
        recreateIndex();
    }





    // =======================================================================================================================


    @Test
    void restCallWillExecute_whenQueryingElastic() {
        assertThat(elasticsearchContainer.isRunning()).isTrue();
        assertThat(api.getAllTransactions()).isNotNull();
        assertThat(api.getAllTransactions()).isEqualTo(Collections.emptyList());
    }



// =======================================================================================================================

    // specific transaction tests
    @Test
    void whenQueryingSpecificTransaction_andExists_returnsTransactionToString()  {

        BankTransaction transaction = BankTransaction.builder()
                .id("1")
                .memo("test memo")
                .date(new Date())
                .amount(Double.valueOf("123.11"))
                .debtorAccountNumber(1L)
                .creditorAccountNumber(2L)
                .build();

        template.save(transaction);
        String specificTransaction = api.getSpecificTransaction(transaction.getId());

        assertThat(specificTransaction).contains(transaction.getId(), transaction.getMemo(), transaction.getDebtorAccountNumber().toString(), transaction.getCreditorAccountNumber().toString());

    }

    @Test
    void whenQueryingSpecificTransaction_andDoesNotExist_returnsErrorMessage() {
        assertThat(api.getSpecificTransaction("22")).isEqualTo("Could not find transaction with id - " + "22");
    }



    /* GREATEST TRANSACTION BY AMOUNT */
// =======================================================================================================================




    @Test
    void queryWillReturnGreatestTransaction()  {
        List<BankTransaction> listOfTransactions = getListOfTransactions();
        template.save(listOfTransactions);
        template.indexOps(BankTransaction.class).refresh();
        BankTransaction transaction = listOfTransactions.get(2);
        assertThat(api.getTransactionWithMaxAmount()).contains(transaction.getId());
    }


    @Test
    void whenNoItemsPersisted_errorMessage_onQueryingGreatestTransfer() {
        assertThat(api.getTransactionWithMaxAmount()).isEqualTo("No transactions have been persisted yet.");
    }





    /* LOWEST TRANSACTION BY AMOUNT */
// =======================================================================================================================
    @Test
    void queryWillReturnLowestTransaction()  {
        List<BankTransaction> listOfTransactions = getListOfTransactions();
        template.save(listOfTransactions);
        template.indexOps(BankTransaction.class).refresh();
        BankTransaction transaction = listOfTransactions.get(4);
        assertThat(api.getTransactionWithMinAmount()).contains(transaction.getId());

    }


    @Test
    void whenNoItemsPersisted_errorMessage_onQueryingLowestMessage() {
        assertThat(api.getTransactionWithMinAmount()).isEqualTo("No transactions have been persisted yet.");
    }





    /* GREATER THAN SPECIFIED AMOUNT TESTS */
// =======================================================================================================================



    //GREATER THAN TEST

    @Test
    void queryWillReturnAllTransactions_greaterThanAmountSpecified() {
        template.save(getListOfTransactions());
        template.indexOps(BankTransaction.class).refresh();
        assertThat(api.transactionsWithAmountGT("150")).hasSize(3);
    }


    @Test
    void searchingGTAmountTransactions_willReturnEmptyList_whenNoneFound() {
        assertThat(api.transactionsWithAmountGT("1.00")).isEmpty();
    }







    /* LESS THAN SPECIFIED AMOUNT TESTS */
// =======================================================================================================================

    @Test
    void queryWillReturnAllTransactions_lessThanAmountSpecified()  {
        for (BankTransaction transaction : getListOfTransactions()) {
            template.save(transaction);
        }
        template.indexOps(BankTransaction.class).refresh();
        assertThat(api.transactionsWithAmountLT("400.0")).hasSize(5);
    }


    @Test
    void searchingLTAmountTransactions_willReturnEmptyList_whenNoneFound() {
        assertThat(api.transactionsWithAmountLT("1000.00")).isEmpty();
    }



// =======================================================================================================================





    private void recreateIndex() {
        if (template.indexOps(BankTransaction.class).exists()) {
            template.indexOps(BankTransaction.class).delete();
        }
        template.indexOps(BankTransaction.class).create();
    }





    private List<BankTransaction> getListOfTransactions() {
        BankTransaction transaction = BankTransaction.builder()
                .id("11")
                .amount(100.00)
                .date(new Date())
                .memo("100 bucks memo")
                .creditorAccountNumber(1111L)
                .debtorAccountNumber(222222L)
                .origin("postgres.bank")
                .build();

        BankTransaction transaction2 = BankTransaction.builder()
                .id("22")
                .amount(200.00)
                .date(new Date())
                .memo("200 bucks memo")
                .creditorAccountNumber(134111L)
                .debtorAccountNumber(222223212L)
                .origin("postgres2.bank")
                .build();


        BankTransaction transaction3 = BankTransaction.builder()
                .id("33")
                .amount(300.00)
                .date(new Date())
                .memo("300 bucks memo")
                .creditorAccountNumber(124111L)
                .debtorAccountNumber(2465432L)
                .origin("postgres.bank")
                .build();


        BankTransaction transaction4 = BankTransaction.builder()
                .id("44")
                .amount(250.00)
                .date(new Date())
                .memo("250 bucks memo")
                .creditorAccountNumber(11321121L)
                .debtorAccountNumber(223411242L)
                .origin("postgres.bank")
                .build();

        BankTransaction transaction5 = BankTransaction.builder()
                .id("55")
                .amount(44.36)
                .date(new Date())
                .memo("44 bucks memo")
                .creditorAccountNumber(1786111L)
                .debtorAccountNumber(222267622L)
                .origin("postgres.bank")
                .build();

        return Arrays.asList(transaction, transaction2, transaction3, transaction4, transaction5);

    }



}
