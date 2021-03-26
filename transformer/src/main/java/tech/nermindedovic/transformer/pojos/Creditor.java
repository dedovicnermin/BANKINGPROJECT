package tech.nermindedovic.transformer.pojos;

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
