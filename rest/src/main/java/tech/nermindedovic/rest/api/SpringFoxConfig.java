package tech.nermindedovic.rest.api;



import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


@Configuration
@Slf4j
public class SpringFoxConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(handlerPredicate())
                .paths(PathSelectors.any())
                .build();
    }

    private Predicate<RequestHandler> handlerPredicate() {
        return RequestHandlerSelectors.withClassAnnotation(RestController.class);
    }


    @Primary
    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider() {
        return () -> {
            SwaggerResource wsResource = new SwaggerResource();
            wsResource.setName("BANKING DEFINITION");
            wsResource.setSwaggerVersion("3.0");
            wsResource.setLocation("/openapi.yaml");
            List<SwaggerResource> resources = new ArrayList<>();
            resources.add(wsResource);
            return resources;
        };
    }




}
