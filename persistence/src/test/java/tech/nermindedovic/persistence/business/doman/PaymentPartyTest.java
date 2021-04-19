package tech.nermindedovic.persistence.business.doman;

import org.junit.jupiter.api.Test;
import tech.nermindedovic.persistence.data.entity.Account;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentPartyTest {

    @Test
    void dataAnnotationOnPaymentParty_test() {
        Account debtorAccount = new Account(1L, 1L, "Sharon", BigDecimal.TEN);
        Account creditorAccount = new Account(2L, 2L, "Kelly", BigDecimal.TEN);
        PaymentParty paymentParty = new PaymentParty(debtorAccount, creditorAccount);

        assertEquals(paymentParty.getCreditorAccount(), creditorAccount);
        assertEquals(paymentParty.getDebtorAccount(), debtorAccount);

    }

}