package tech.nermindedovic.library.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Creditor {
    private long accountNumber;
    private long routingNumber;
}
