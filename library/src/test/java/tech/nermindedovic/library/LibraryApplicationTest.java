package tech.nermindedovic.library;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.nermindedovic.library.pojos.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryApplicationTest {

    @Test
    void pojos() {
        BalanceMessage balanceMessage = new BalanceMessage(1L, 222L, "", false);
        Creditor creditor = new Creditor(44L, 222L);
        Debtor debtor = new Debtor(11L, 111L);
        TransferMessage transferMessage = TransferMessage.builder()
                .creditor(creditor)
                .debtor(debtor)
                .amount(BigDecimal.TEN)
                .date(LocalDate.now())
                .memo("memo")
                .messageId(UUID.randomUUID().getLeastSignificantBits())
                .build();

        TransferValidation validation = TransferValidation.builder()
                .creditorAccount(creditor)
                .debtorAccount(debtor)
                .amount(BigDecimal.TEN)
                .currentLeg(1)
                .messageId(34241L)
                .transferMessage("TransferMessage XML")
                .build();

        Assertions.assertAll(
                () -> assertThat(TransferStatus.FAIL.name()).containsSequence("FAIL"),
                () -> assertThat(TransferStatus.PROCESSING.name()).containsSequence("PROCESSING"),
                () -> assertThat(TransferStatus.PERSISTED.name()).containsSequence("PERSISTED"),
                () -> assertThat(balanceMessage.getErrors()).isFalse(),
                () -> assertThat(validation.getAmount()).isEqualTo(transferMessage.getAmount())
        );




    }

}
