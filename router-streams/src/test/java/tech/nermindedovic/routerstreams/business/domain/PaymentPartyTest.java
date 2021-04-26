package tech.nermindedovic.routerstreams.business.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PaymentPartyTest {

    @Test
    void invalidRoutingNumbersPresent_notPresent() {
        PaymentParty paymentParty = PaymentParty.builder()
                .debtorAccount(new Account(123L, 111L))
                .creditorAccount(new Account(456L, 222L))
                .build();

        assertThat(paymentParty.invalidRoutingNumbersPresent()).isFalse();

    }

    @Test
    void invalidRoutingNumbersPresent_present() {
        PaymentParty paymentParty = PaymentParty.builder()
                .debtorAccount(new Account(123L, 789L))
                .creditorAccount(new Account(456L, 222L))
                .build();
        assertThat(paymentParty.invalidRoutingNumbersPresent()).isTrue();
    }

    @Test
    void getRoutingSet() {
        PaymentParty paymentParty = PaymentParty.builder()
                .debtorAccount(new Account(123L, 111L))
                .creditorAccount(new Account(456L, 222L))
                .build();
        assertThat(paymentParty.getRoutingSet()).hasSize(2);
        assertThat(paymentParty.getRoutingSet()).contains(111L, 222L);
    }
}