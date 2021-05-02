package tech.nermindedovic.library.pojos;

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
    @JsonProperty("messageId")
    private Long messageId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currentLeg")
    private int currentLeg = 1;

    @JsonProperty("transferMessage")
    private String transferMessage;

    @JsonProperty("debtorAccount")
    private Debtor debtorAccount;

    @JsonProperty("creditorAccount")
    private Creditor creditorAccount;


}
