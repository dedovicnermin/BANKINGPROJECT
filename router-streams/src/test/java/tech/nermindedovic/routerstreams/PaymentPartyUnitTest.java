package tech.nermindedovic.routerstreams;

import org.junit.jupiter.api.Test;
import tech.nermindedovic.routerstreams.business.domain.Account;
import tech.nermindedovic.routerstreams.business.domain.PaymentParty;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentPartyUnitTest {

    @Test
    void onValidRoutingNumbers_checkWillReturnFalse() {
        PaymentParty party = PaymentParty.builder()
                .messageId(111111)
                .creditorAccount(new Account(123, 111))
                .debtorAccount(new Account(456, 222))
                .build();
        assertThat(party.invalidRoutingNumbersPresent()).isFalse();
    }


    @Test
    void onInvalidRoutingNumbers_checkWillReturnTrue() {
        PaymentParty party = PaymentParty.builder()
                .messageId(111111)
                .creditorAccount(new Account(123, 111))
                .debtorAccount(new Account(567, 859))
                .build();
        assertThat(party.invalidRoutingNumbersPresent()).isTrue();
    }

    @Test
    void whenAccountsHaventBeenPopulatedByParser_checkWillReturnTrue() {
        PaymentParty paymentParty = new PaymentParty();
        assertThat(paymentParty.invalidRoutingNumbersPresent()).isTrue();
    }

    @Test
    void routingSetWillTakeRoutingsFromAccounts() {
        PaymentParty party = PaymentParty.builder()
                .messageId(111111)
                .creditorAccount(new Account(123, 111))
                .debtorAccount(new Account(456, 222))
                .build();
        Set<Long> routingSet = party.getRoutingSet();
        assertThat(routingSet.size()).isEqualTo(2);
        assertThat(routingSet.contains(111L)).isTrue();
        assertThat(routingSet.contains(222L)).isTrue();
    }

}
