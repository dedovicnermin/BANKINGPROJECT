package tech.nermindedovic.rest.api;

import org.junit.jupiter.api.Test;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import static org.assertj.core.api.Assertions.assertThat;

class SpringFoxConfigTest {

    @Test
    void swaggerResourcesProvider() {
        SpringFoxConfig config = new SpringFoxConfig();
        SwaggerResourcesProvider resourcesProvider = config.swaggerResourcesProvider();
        assertThat(resourcesProvider.get().size()).isEqualTo(3);
    }
}