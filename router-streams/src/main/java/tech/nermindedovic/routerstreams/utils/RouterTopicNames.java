package tech.nermindedovic.routerstreams.utils;

public final class RouterTopicNames {
    private RouterTopicNames() {}


    //Balance Request
    public static final String INBOUND_BALANCE_REQUEST_TOPIC = "balance.update.request";
    public static final String OUTBOUND_BALANCE_REQUEST_PREFIX = "balance.update.request.";
    public static final String OUTBOUND_BALANCE_RETURN_TOPIC = "balance.update.response";

    //processInitialTransfer-INPUT
    public static final String INBOUND_INITIAL_TRANSFER_TOPIC = "funds.transfer.request";
    public static final String TRANSFER_ERROR_HANDLER_TOPIC = "router.trsf.error.handler";              // [0]
    public static final String TRANSFER_SINGLEBANK_PROCESSOR = "router.trsf.singlebank.processor";      // [1]
    public static final String TRANSFER_DOUBLEBANK_PROCESSOR = "router.trsf.doublebank.processor";      // [2]



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
    public static final String TRANSFER_STATUS_STORE = "transfer.status.store";












    public static final String VALIDATION_ERROR_HANDLER_TOPIC = "router.validation.error.handler";



    public static final String TRANSFER_STATUS_FAILED_HANDLER = "router.metrics.handler-failed";
    public static final String TRANSFER_STATUS_PROCESSING_SINGLE_HANDLER = "router.metrics.handler.single-processing";
    public static final String TRANSFER_STATUS_PROCESSING_DOUBLE_HANDLER = "router.metrics.handler.double-processing";
    public static final String TRANSFER_STATUS_SUCCESS_HANDLER = "router.metrics.handler-persist";

    public static final String VALIDATED_PREPARE_FANOUT_TOPIC = "router.validated.prepare.fanout";
    public static final String VALIDATED_FANOUT_TOPIC = "router.validated.fanout";


    public static final String TRANSFER_XML_REGISTER = "transfer.xml.store.register";
    public static final String TRANSFER_XML_TABLE_TOPIC = "transfer.xml.store.table";
    public static final String TRANSFER_XML_STORE_NAME = "transfer.xml.store";
    public static final String TRANSFER_XML_STORE_OUTPUT = "transfer.xml.store.output";




}
