package tech.nermindedovic.routerstreams.business.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentParty {
    private long messageId;
    private BigDecimal amount;
    private Account debtorAccount;
    private Account creditorAccount;


    public boolean invalidRoutingNumbersPresent() {
        if (debtorAccount == null || creditorAccount == null) return true;
        return Stream.of(debtorAccount.getRoutingNumber(), creditorAccount.getRoutingNumber()).anyMatch(routing -> (!(routing.equals(111L) || routing.equals(222L))));
    }


    public Set<Long> getRoutingSet() {
        return Stream.of(debtorAccount.getRoutingNumber(), creditorAccount.getRoutingNumber())
                .collect(Collectors.toSet());
    }

}
