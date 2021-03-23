package tech.nermindedovic.persistence.data.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AccountAttributesTest {

    @Test
    void test_accountAttributes_willReturn_correctConstants() {
        assertAll(() -> {
            assertThat(AccountAttributes.BALANCE).isEqualTo("BALANCE");
            assertThat(AccountAttributes.ACCT_NUM).isEqualTo("ACCOUNT_NUMBER");
            assertThat(AccountAttributes.NAME).isEqualTo("USER_NAME");
            assertThat(AccountAttributes.ROUT_NUM).isEqualTo("ROUTING_NUMBER");
        });
    }

}