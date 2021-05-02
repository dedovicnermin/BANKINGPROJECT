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
    void transferValidation_test() {
        long messageId = 1;
        BigDecimal amount = BigDecimal.TEN;
        int leg = 1;
        String transferMessage = "Mock TransferMessage XML";
        Creditor creditor = new Creditor(44L, 222L);
        Debtor debtor = new Debtor(11L, 111L);


        TransferValidation transferValidation = new TransferValidation();

        transferValidation.setMessageId(messageId);
        transferValidation.setAmount(amount);
        transferValidation.setCurrentLeg(leg);
        transferValidation.setTransferMessage(transferMessage);
        transferValidation.setCreditorAccount(creditor);
        transferValidation.setDebtorAccount(debtor);

        TransferValidation validationAllArgs = new TransferValidation(messageId, amount, leg, transferMessage, debtor, creditor);

        Assertions.assertAll(
                () -> assertThat(validationAllArgs.getMessageId()).isEqualTo(messageId),
                () -> assertThat(validationAllArgs.getAmount()).isEqualTo(amount),
                () -> assertThat(validationAllArgs.getCurrentLeg()).isEqualTo(leg),
                () -> assertThat(validationAllArgs.getTransferMessage()).isEqualTo(transferMessage),
                () -> assertThat(validationAllArgs.getCreditorAccount()).isEqualTo(creditor),
                () -> assertThat(validationAllArgs.getDebtorAccount()).isEqualTo(debtor),

                () -> assertThat(transferValidation.getMessageId()).isEqualTo(messageId),
                () -> assertThat(transferValidation.getAmount()).isEqualTo(amount),
                () -> assertThat(transferValidation.getCurrentLeg()).isEqualTo(leg),
                () -> assertThat(transferValidation.getTransferMessage()).isEqualTo(transferMessage),
                () -> assertThat(transferValidation.getCreditorAccount()).isEqualTo(creditor),
                () -> assertThat(transferValidation.getDebtorAccount()).isEqualTo(debtor)
        );

    }


    @Test
    void transferMessage_test() {
        long messageId = 100;
        Creditor creditor = new Creditor(44L, 222L);
        Debtor debtor = new Debtor(11L, 111L);
        LocalDate date = LocalDate.now();
        BigDecimal amount = BigDecimal.TEN;
        String memo = "A funds transfer memo";

        TransferMessage transferMessageAllArgs = new TransferMessage(messageId, creditor, debtor, date, amount, memo);
        TransferMessage transferMessage = new TransferMessage();
        transferMessage.setMessageId(messageId);
        transferMessage.setCreditor(creditor);
        transferMessage.setDebtor(debtor);
        transferMessage.setDate(date);
        transferMessage.setAmount(amount);
        transferMessage.setMemo(memo);

        Assertions.assertAll(
                () -> assertThat(transferMessageAllArgs.getMessageId()).isEqualTo(messageId),
                () -> assertThat(transferMessageAllArgs.getCreditor()).isEqualTo(creditor),
                () -> assertThat(transferMessageAllArgs.getDebtor()).isEqualTo(debtor),
                () -> assertThat(transferMessageAllArgs.getDate()).isEqualTo(date),
                () -> assertThat(transferMessageAllArgs.getAmount()).isEqualTo(amount),
                () -> assertThat(transferMessageAllArgs.getMemo()).isEqualTo(memo),

                () -> assertThat(transferMessage.getMessageId()).isEqualTo(messageId),
                () -> assertThat(transferMessage.getCreditor()).isEqualTo(creditor),
                () -> assertThat(transferMessage.getDebtor()).isEqualTo(debtor),
                () -> assertThat(transferMessage.getDate()).isEqualTo(date),
                () -> assertThat(transferMessage.getAmount()).isEqualTo(amount),
                () -> assertThat(transferMessage.getMemo()).isEqualTo(memo)
        );
    }

    @Test
    void debtor_creditor_test() {
        long debtorAN = 99L, debtorRN = 111L;
        long creditorAN = 44L, creditorRN = 222L;


        Debtor debtor = new Debtor();
        debtor.setAccountNumber(debtorAN);
        debtor.setRoutingNumber(debtorRN);

        Creditor creditor = new Creditor();
        creditor.setAccountNumber(creditorAN);
        creditor.setRoutingNumber(creditorRN);

        Assertions.assertAll(
                () -> assertThat(debtor.getAccountNumber()).isEqualTo(debtorAN),
                () -> assertThat(debtor.getRoutingNumber()).isEqualTo(debtorRN),
                () -> assertThat(creditor.getAccountNumber()).isEqualTo(creditorAN),
                () -> assertThat(creditor.getRoutingNumber()).isEqualTo(creditorRN)
        );
    }


    @Test
    void balanceMessage_test() {
        long accountNumber = 38383, routingNumber = 33333333;
        String balance = "100.00";

        BalanceMessage balanceMessage = new BalanceMessage();
        balanceMessage.setAccountNumber(accountNumber);
        balanceMessage.setRoutingNumber(routingNumber);
        balanceMessage.setBalance(balance);
        balanceMessage.setErrors(true);
        BalanceMessage balanceMessageAllArgs = new BalanceMessage(accountNumber, routingNumber, balance, false);


        Assertions.assertAll(
                () -> assertThat(balanceMessage.getAccountNumber()).isEqualTo(accountNumber),
                () -> assertThat(balanceMessage.getRoutingNumber()).isEqualTo(routingNumber),
                () -> assertThat(balanceMessage.getBalance()).isEqualTo(balance),
                () -> assertThat(balanceMessage.getErrors()).isTrue(),


                () -> assertThat(balanceMessageAllArgs.getAccountNumber()).isEqualTo(accountNumber),
                () -> assertThat(balanceMessageAllArgs.getRoutingNumber()).isEqualTo(routingNumber),
                () -> assertThat(balanceMessageAllArgs.getBalance()).isEqualTo(balance),
                () -> assertThat(balanceMessageAllArgs.getErrors()).isFalse()
        );
    }

    @Test
    void pojo_test() {
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
