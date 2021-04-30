package tech.nermindedovic.routerstreams.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdom2.input.SAXBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SAXBuilder saxBuilder() {
        return new SAXBuilder();
    }



}
