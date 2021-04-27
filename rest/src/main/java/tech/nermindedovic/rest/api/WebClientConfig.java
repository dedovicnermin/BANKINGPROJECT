package tech.nermindedovic.rest.api;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient routerApiClient() {
        return WebClient.create("http://localhost:8084/transfer");
    }

}
