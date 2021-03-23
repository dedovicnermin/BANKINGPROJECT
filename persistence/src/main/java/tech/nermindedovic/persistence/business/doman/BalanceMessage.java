package tech.nermindedovic.persistence.business.doman;

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


}
