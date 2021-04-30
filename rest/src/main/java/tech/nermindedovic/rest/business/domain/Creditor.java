package tech.nermindedovic.rest.business.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Creditor {

    @NotNull
    private long accountNumber;

    @NotNull
    private long routingNumber;

}
