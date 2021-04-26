package tech.nermindedovic.routerstreams.utils;

public final class RouterTopicNames {
    private RouterTopicNames() {}

    // Transfer Request
    public static final String INBOUND_INITIAL_TRANSFER_TOPIC = "funds.transfer.request";
    public static final String OUTBOUND_TRANSFER_ERROR = "funds.transfer.error";

    // 1 bank processing
    public static final String OUTBOUND_SINGLE_BANK_PREFIX = "funds.transfer.";

    // 2 banks processing
    public static final String INBOUND_VALIDATION_TOPIC = "router.validate.transfer";
    public static final String OUTBOUND_VALIDATION_PREFIX = "funds.validate.";
    public static final String OUTBOUND_FUNDS_SINGLE_ACCOUNT_PREFIX = "funds.transfer.single.";


    // topics for state store
    public static final String INBOUND_TRANSFER_DATA_TOPIC = "funds.transfer.status";
    public static final String OUTBOUND_TRANSFER_DATA_TOPIC = "transfer.status";
    public static final String TRANSFER_STORE = "transfer.status.store";





    //Balance Request
    public static final String INBOUND_BALANCE_REQUEST_TOPIC = "balance.update.request";
    public static final String OUTBOUND_BALANCE_REQUEST_PREFIX = "balance.update.request.";





}
