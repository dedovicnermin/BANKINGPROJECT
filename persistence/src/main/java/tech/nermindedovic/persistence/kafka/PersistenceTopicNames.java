package tech.nermindedovic.persistence.kafka;






public final class PersistenceTopicNames  {
    private PersistenceTopicNames() {}

    public static final String INBOUND_TRANSFER_REQUEST =  "${persistence-topics.INBOUND_TRANSFER_REQUEST}";
    public static final String INBOUND_TRANSFER_VALIDATION =  "${persistence-topics.INBOUND_TRANSFER_VALIDATION}";
    public static final String INBOUND_TRANSFER_SINGLE_USER =  "${persistence-topics.INBOUND_TRANSFER_SINGLE_USER}";
    public static final String INBOUND_BALANCE_REQUEST =  "${persistence-topics.INBOUND_BALANCE_REQUEST}";
    public static final String OUTBOUND_TRANSFER_ERRORS =  "${persistence-topics.OUTBOUND_TRANSFER_ERRORS}";
    public static final String OUTBOUND_TRANSFER_STATUS =  "${persistence-topics.OUTBOUND_TRANSFER_STATUS}";
    public static final String OUTBOUND_ROUTER_VALIDATION =  "${persistence-topics.OUTBOUND_ROUTER_VALIDATION}";
    public static final String OUTBOUND_BALANCE_RESPONSE =  "${persistence-topics.OUTBOUND_BALANCE_RESPONSE}";









}
