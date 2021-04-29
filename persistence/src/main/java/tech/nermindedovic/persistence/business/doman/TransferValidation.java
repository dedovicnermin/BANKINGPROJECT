package tech.nermindedovic.persistence.business.doman;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;
import tech.nermindedovic.persistence.exception.InvalidTransferValidationException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonRootName("TransferValidation")
public class TransferValidation {
    @JsonProperty(value = "messageId", required = true)
    private long messageId;

    @JsonProperty(value = "amount", required = true)
    private BigDecimal amount;

    @JsonProperty("currentLeg")
    private int currentLeg = 1;

    @JsonProperty(value = "transferMessage", required = true)
    private String transferMessage;

    @JsonProperty(value = "debtorAccount", required = true)
    private Account debtorAccount;

    @JsonProperty(value = "creditorAccount", required = true)
    private Account creditorAccount;


    public String toJsonString() {
        try {
            if (!requiredDataIsPresent()) throw new InvalidTransferValidationException("Invalid information provided on TransferValidation.");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException | InvalidTransferValidationException e) {
            return "error";
        }
    }

    private boolean requiredDataIsPresent() {
        return (
                messageId != 0 &&
                creditorAccount != null &&
                debtorAccount != null &&
                !transferMessage.isEmpty() &&
                amount != null
                );
    }




}
