package tech.nermindedovic.rest.api;


import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
@Slf4j
public class WebClientConfig extends AbstractElasticsearchConfiguration {

    @Value("${router-endpoint:localhost:8082}")
    private String routerEndpoint;

    @Value("${elastic-endpoint:localhost:9200}")
    private String elasticEndpoint;

    @Value("${ksql-endpoint:localhost:8088}")
    private String ksqlEndpoint;


    @Bean
    public WebClient routerApiClient() {
        return WebClient.create("http://" + routerEndpoint + "/transfer");
    }


    @Bean
    public RestHighLevelClient elasticsearchClient() {
        log.info("\n" + elasticEndpoint + "\n");
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(elasticEndpoint)
                .build();

        return RestClients.create(clientConfiguration).rest();
    }



    @Bean
    public Client ksqlClient() {
        log.info(ksqlEndpoint);
        String[] split = ksqlEndpoint.split(":");
        ClientOptions options = ClientOptions.create()
                .setHost(split[0])
                .setPort(Integer.parseInt(split[1]));
        return Client.create(options);
    }











}
