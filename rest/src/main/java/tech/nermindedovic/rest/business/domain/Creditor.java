package tech.nermindedovic.rest.business.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Creditor {

    private long accountNumber;
    private long routingNumber;

}
