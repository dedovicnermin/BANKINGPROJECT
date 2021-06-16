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

    private static final String SWAGGER_VERSION = "3.0";

    private static final String REST_DEFINITION = "BANKING DEFINITION";
    private static final String REST_LOCATION = "/restApi.yaml";

    private static final String KSQL_DEFINITION = "KSQL DEFINITION";
    private static final String KSQL_LOCATION = "/ksqlApi.yaml";

    private static final String ELASTIC_DEFINITION = "ELASTICSEARCH DEFINITION";
    private static final String ELASTIC_LOCATION = "/elasticApi.yaml";


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
            List<SwaggerResource> resources = new ArrayList<>();

            SwaggerResource restApi = createResource(REST_DEFINITION, REST_LOCATION);
            SwaggerResource ksqlApi = createResource(KSQL_DEFINITION, KSQL_LOCATION);
            SwaggerResource elasticApi = createResource(ELASTIC_DEFINITION, ELASTIC_LOCATION);

            resources.add(restApi);
            resources.add(ksqlApi);
            resources.add(elasticApi);

            return resources;
        };
    }


    private SwaggerResource createResource(final String definition, final String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(definition);
        swaggerResource.setSwaggerVersion(SWAGGER_VERSION);
        swaggerResource.setLocation(location);
        return swaggerResource;
    }




}
