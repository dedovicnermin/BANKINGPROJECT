package tech.nermindedovic.routerstreams.utils;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Produced;

public final class RouterAppUtils {
    private RouterAppUtils() {}

    public static final String BALANCE_ERROR_XML = "<BalanceMessage><accountNumber>0</accountNumber><routingNumber>0</routingNumber><balance></balance><errors>true</errors></BalanceMessage>";
    public static final String TRANSFER_ERROR_PREFIX = "ERROR ROUTING MESSAGE - ";
    public static final String VALIDATION_ERROR_PREFIX = "ERROR VALIDATING MESSAGE - ";

    public static final Produced<String, String> producedWithStringSerdes = Produced.with(Serdes.String(), Serdes.String());

}
