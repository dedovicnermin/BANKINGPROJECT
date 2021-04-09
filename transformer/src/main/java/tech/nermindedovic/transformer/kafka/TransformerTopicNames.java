package tech.nermindedovic.transformer.kafka;

public final class TransformerTopicNames {
    private TransformerTopicNames() {}

    /**
     * <inbound | outbound >_<destination>_<request | response>
     */

    public static final String INBOUND_REST_BALANCE = "balance.transformer.request";
    public static final String OUTBOUND_REST_BALANCE = "balance.transformer.response";
    //^ inside the header of consumed message, hence @SendTo (no destination specified)

    public static final String OUTBOUND_PERSISTENCE_BALANCE =  "balance.update.request";
    public static final String INBOUND_PERSISTENCE_BALANCE = "balance.update.response";



    public static final String INBOUND_REST_TRANSFER = "funds.transformer.request";
    public static final String OUTBOUND_PERSISTENCE_TRANSFER = "funds.transfer.request";



    public static final String OUTBOUND_TRANSFER_ERRORS = "funds.transfer.error";

}
