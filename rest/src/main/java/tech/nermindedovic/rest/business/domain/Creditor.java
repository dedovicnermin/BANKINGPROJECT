package tech.nermindedovic.rest.business.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class Creditor {

    @NotNull
    private long accountNumber;

    @NotNull
    private long routingNumber;

}
