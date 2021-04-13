package tech.nermindedovic.persistence.kafka;

public final class PersistenceTopicNames {
    private PersistenceTopicNames() {}

    public static final String INBOUND_TRANSFER_REQUEST = "funds.transfer.request";
    public static final String INBOUND_BALANCE_REQUEST = "balance.update.request";
    public static final String OUTBOUND_TRANSFER_ERRORS = "funds.transfer.error";

}
