package tech.nermindedovic.library.kafka;

import java.util.HashMap;
import java.util.Map;

public final class KafkaSecurityUtils {
    private KafkaSecurityUtils() {}

    public static final String SSL = "SSL";
    public static final String PROTOCOL_PROP = "security.protocol";
    public static final String TRUSTSTORE_LOCATION_PROP = "ssl.truststore.location";
    public static final String KEYSTORE_LOCATION_PROP = "ssl.keystore.location";
    public static final String KEY_PASS_PROP = "ssl.key.password";
    public static final String KEYSTORE_PASS_PROP = "ssl.keystore.password";
    public static final String TRUSTSTORE_PASS_PROP = "ssl.truststore.password";
    public static final String ENDPOINT_IDENTITY_PROP = "ssl.endpoint.identification.algorithm";



    public static Map<String, Object> getSecurityConfiguration(final String keystore, final String truststore, final String sslPassword) {
        Map<String, Object> config = new HashMap<>();
        config.put(PROTOCOL_PROP, SSL);
        config.put(TRUSTSTORE_LOCATION_PROP, truststore);
        config.put(TRUSTSTORE_PASS_PROP, sslPassword);
        config.put(KEY_PASS_PROP, sslPassword);
        config.put(KEYSTORE_PASS_PROP, sslPassword);
        config.put(KEYSTORE_LOCATION_PROP, keystore);
        config.put(ENDPOINT_IDENTITY_PROP, "");
        return config;
    }
}
