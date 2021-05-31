package tech.nermindedovic.rest.api.elastic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.nermindedovic.rest.api.RestAPI;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@EmbeddedKafka(partitions = 1)
@ActiveProfiles("test")
@DirtiesContext
@Testcontainers
@Slf4j
class ElasticIntegrationTest {
    private static final String ELASTIC_VERSION = "7.9.3";

    @Container
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch-oss" + ":" + ELASTIC_VERSION);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("elastic-endpoint", elasticsearchContainer::getHttpHostAddress);
    }

    @Autowired ElasticsearchRestTemplate template;

    @BeforeAll
    static void setup() {
        elasticsearchContainer.start();
    }

    @AfterAll
    static void shutdown() {
        elasticsearchContainer.stop();
    }



    @Autowired RestAPI restAPI;

    @Test
    void restCallWillExecute() {
        assertThat(elasticsearchContainer.isRunning()).isTrue();
        template.indexOps(BankTransaction.class).create();
        assertThat(restAPI.getAllTransactions()).isNotNull();
        assertThat(restAPI.getAllTransactions()).isInstanceOf(SearchHits.class);
    }








}
