package tech.nermindedovic.transformer.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BalanceMessage {

    private long accountNumber;
    private long routingNumber;

    private String balance;
    private boolean errors;


    public boolean getErrors() {
        return errors;
    }
}
