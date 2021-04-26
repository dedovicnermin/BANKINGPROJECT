package tech.nermindedovic.persistence.kafka;

public final class PersistenceTopicNames {
    private PersistenceTopicNames() {}

    public static final String INBOUND_TRANSFER_REQUEST = "funds.transfer.111";
    public static final String INBOUND_TRANSFER_VALIDATION = "funds.validate.111";
    public static final String INBOUND_TRANSFER_SINGLE_USER = "funds.transfer.single.111";
    public static final String INBOUND_BALANCE_REQUEST = "balance.update.request.111";
    public static final String OUTBOUND_TRANSFER_ERRORS = "funds.transfer.error";

    public static final String OUTBOUND_ROUTER_VALIDATION = "router.validate.transfer";


}
