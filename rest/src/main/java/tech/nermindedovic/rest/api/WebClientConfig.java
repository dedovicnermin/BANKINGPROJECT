package tech.nermindedovic.rest.api;


import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig extends AbstractElasticsearchConfiguration {

    @Value("${router-endpoint:localhost:8082}")
    private String routerEndpoint;

    @Value("${elastic-endpoint:localhost:9200}")
    private String elasticEndpoint;


    @Bean
    public WebClient routerApiClient() {
        return WebClient.create("http://" + routerEndpoint + "/transfer");
    }


    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(elasticEndpoint)
                .build();

        return RestClients.create(clientConfiguration).rest();
    }






}
