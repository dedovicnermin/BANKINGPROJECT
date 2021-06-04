package tech.nermindedovic.routerstreams.config.serdes;

import org.apache.kafka.common.serialization.Serdes;
import tech.nermindedovic.library.kafka.JsonDeserializer;
import tech.nermindedovic.library.kafka.JsonSerializer;
import tech.nermindedovic.library.pojos.TransferStatus;
import tech.nermindedovic.library.pojos.TransferValidation;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;

public final class CustomSerdes {
    private CustomSerdes() {}

    public static final class PaymentDataSerde extends Serdes.WrapperSerde<PaymentData> {
        public PaymentDataSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>(PaymentData.class));
        }
    }

    public static final class TransferValidationSerde extends Serdes.WrapperSerde<TransferValidation> {
        public TransferValidationSerde() {super(new JsonSerializer<>(), new JsonDeserializer<>(TransferValidation.class));}
    }

    public static final class TransferStatusSerde extends Serdes.WrapperSerde<TransferStatus> {
        public TransferStatusSerde() { super(new JsonSerializer<>(), new JsonDeserializer<>(TransferStatus.class)); }
    }
}
