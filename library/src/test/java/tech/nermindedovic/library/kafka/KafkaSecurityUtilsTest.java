package tech.nermindedovic.library.kafka;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class KafkaSecurityUtilsTest {

    @Test
    void getSecurityConfiguration() {
        String truststore = "/path/to/truststore.jks";
        String keystore = "/path/to/keystore.jks";
        String password = "password";

        Map<String, Object> securityConfiguration = KafkaSecurityUtils.getSecurityConfiguration(keystore, truststore, password);
        assertAll(
                () -> assertThat(securityConfiguration).containsEntry(KafkaSecurityUtils.PROTOCOL_PROP, KafkaSecurityUtils.SSL),
                () -> assertThat(securityConfiguration).containsEntry(KafkaSecurityUtils.TRUSTSTORE_LOCATION_PROP, truststore),
                () -> assertThat(securityConfiguration).containsEntry(KafkaSecurityUtils.KEYSTORE_LOCATION_PROP,keystore),
                () -> assertThat(securityConfiguration).containsEntry(KafkaSecurityUtils.TRUSTSTORE_PASS_PROP, password),
                () -> assertThat(securityConfiguration).containsEntry(KafkaSecurityUtils.KEYSTORE_PASS_PROP, password),
                () -> assertThat(securityConfiguration).containsEntry(KafkaSecurityUtils.KEY_PASS_PROP, password),
                () -> assertThat(securityConfiguration).containsEntry(KafkaSecurityUtils.ENDPOINT_IDENTITY_PROP, "")
        );

    }
}