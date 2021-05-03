package tech.nermindedovic.rest.api;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${router-endpoint:localhost:8082}")
    private String routerEndpoint;

    @Bean
    public WebClient routerApiClient() {
        return WebClient.create("http://" + routerEndpoint + "/transfer");
    }

}
