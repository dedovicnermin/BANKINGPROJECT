package tech.nermindedovic.library.pojos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonPropertyOrder(value = {"accountNumber", "routingNumber", "balance", "errors"})
public class BalanceMessage {
    @Positive
    private long accountNumber;

    @Positive
    private long routingNumber;

    private String balance;

    private boolean errors;


    public boolean getErrors() {
        return errors;
    }
}
