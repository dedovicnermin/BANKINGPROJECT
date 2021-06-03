package tech.nermindedovic.routerstreams.config.serdes;

import org.apache.kafka.common.serialization.Serdes;
import tech.nermindedovic.library.kafka.JsonDeserializer;
import tech.nermindedovic.library.kafka.JsonSerializer;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;

public final class CustomSerdes {
    private CustomSerdes() {}

    public static final class PaymentDataSerde extends Serdes.WrapperSerde<PaymentData> {
        public PaymentDataSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>(PaymentData.class));
        }
    }
}
