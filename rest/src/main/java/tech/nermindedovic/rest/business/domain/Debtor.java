package tech.nermindedovic.rest.business.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Debtor {
    private long accountNumber;
    private long routingNumber;
}
