package tech.nermindedovic.rest.business.domain;

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
