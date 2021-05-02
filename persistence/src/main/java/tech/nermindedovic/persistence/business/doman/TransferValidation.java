package tech.nermindedovic.persistence.business.doman;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
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
    @JsonProperty(value = "messageId", required = true)
    private long messageId;

    @JsonProperty(value = "amount", required = true)
    private BigDecimal amount;

    @JsonProperty("currentLeg")
    private int currentLeg = 1;

    @JsonProperty(value = "transferMessage", required = true)
    private String transferMessage;

    @JsonProperty(value = "debtorAccount", required = true)
    private Debtor debtorAccount;

    @JsonProperty(value = "creditorAccount", required = true)
    private Creditor creditorAccount;







}
