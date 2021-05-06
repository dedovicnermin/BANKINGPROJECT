package tech.nermindedovic.rest.api;

import org.junit.jupiter.api.Test;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SpringFoxConfigTest {

    @Test
    void swaggerResourcesProvider() {
        SpringFoxConfig config = new SpringFoxConfig();
        SwaggerResourcesProvider resourcesProvider = config.swaggerResourcesProvider();
        assertThat(resourcesProvider.get().size()).isEqualTo(1);
    }
}