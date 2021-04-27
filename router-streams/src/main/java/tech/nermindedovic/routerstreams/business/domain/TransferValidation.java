package tech.nermindedovic.routerstreams.business.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonRootName("TransferValidation")
public class TransferValidation {
    @JsonProperty("messageId")
    private Long messageId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currentLeg")
    private int currentLeg = 1;

    @JsonProperty("transferMessage")
    private String transferMessage;

    @JsonProperty("debtorAccount")
    private Account debtorAccount;

    @JsonProperty("creditorAccount")
    private Account creditorAccount;


    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "error";
        }
    }




}
