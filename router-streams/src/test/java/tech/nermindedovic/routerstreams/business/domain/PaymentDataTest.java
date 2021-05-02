package tech.nermindedovic.routerstreams.business.domain;

import org.junit.jupiter.api.Test;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentDataTest {

//    @Test
//    void invalidRoutingNumbersPresent_notPresent() {
//        PaymentData paymentData = PaymentData.builder()
//                .debtorAccount(new Debtor(123L, 111L))
//                .creditorAccount(new Creditor(456L, 222L))
//                .build();
//
//        assertThat(paymentData.invalidRoutingNumbersPresent()).isFalse();
//
//    }
//
//    @Test
//    void invalidRoutingNumbersPresent_present() {
//        PaymentData paymentData = PaymentData.builder()
//                .debtorAccount(new Account(123L, 789L))
//                .creditorAccount(new Account(456L, 222L))
//                .build();
//        assertThat(paymentData.invalidRoutingNumbersPresent()).isTrue();
//    }
//
//    @Test
//    void getRoutingSet() {
//        PaymentData paymentData = PaymentData.builder()
//                .debtorAccount(new Account(123L, 111L))
//                .creditorAccount(new Account(456L, 222L))
//                .build();
//        assertThat(paymentData.getRoutingSet()).hasSize(2);
//        assertThat(paymentData.getRoutingSet()).contains(111L, 222L);
//    }
}