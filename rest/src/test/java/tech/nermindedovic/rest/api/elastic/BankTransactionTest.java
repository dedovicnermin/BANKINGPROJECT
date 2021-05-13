package tech.nermindedovic.rest.api.elastic;


import org.junit.jupiter.api.Test;


import java.util.Date;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class BankTransactionTest {

    @Test
    void bankTransaction_setsAndGets() {
        BankTransaction bankTransaction = new BankTransaction();
        bankTransaction.setId("12356654645625453");
        bankTransaction.setCreditorAccountNumber(11L);
        bankTransaction.setDebtorAccountNumber(12L);
        bankTransaction.setDate(new Date());
        bankTransaction.setMemo("memo string");
        bankTransaction.setAmount(20.00);
        bankTransaction.setOrigin("origin.topic");

        assertAll(
                () -> assertThat(bankTransaction.getId()).isNotNull(),
                () -> assertThat(bankTransaction.getCreditorAccountNumber()).isNotNull(),
                () -> assertThat(bankTransaction.getDebtorAccountNumber()).isNotNull(),
                () -> assertThat(bankTransaction.getDate()).isNotNull(),
                () -> assertThat(bankTransaction.getOrigin()).isNotNull(),
                () -> assertThat(bankTransaction.getMemo()).isNotNull(),
                () -> assertThat(bankTransaction.getAmount()).isNotNull()
        );
    }

}