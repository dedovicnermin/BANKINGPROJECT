package tech.nermindedovic.persistence.business.doman;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Debtor {
    private long accountNumber;
    private long routingNumber;
}
