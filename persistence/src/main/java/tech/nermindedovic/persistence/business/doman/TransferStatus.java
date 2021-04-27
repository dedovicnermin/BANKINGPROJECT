package tech.nermindedovic.persistence.business.doman;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum TransferStatus {
    FAIL,
    PROCESSING,
    PERSISTED;


    public String toJsonString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }


}




