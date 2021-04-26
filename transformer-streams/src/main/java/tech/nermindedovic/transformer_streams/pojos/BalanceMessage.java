package tech.nermindedovic.transformer_streams.pojos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonPropertyOrder(value = {"accountNumber", "routingNumber", "balance", "errors"})
public class BalanceMessage {

    private long accountNumber;
    private long routingNumber;

    private String balance;
    private boolean errors;


    public boolean getErrors() {
        return errors;
    }
}
