package tech.nermindedovic.persistence.business.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.library.pojos.BalanceMessage;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.library.pojos.TransferMessage;
import tech.nermindedovic.persistence.business.doman.*;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersistenceServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    private PersistenceService persistenceService;

    @Test
    void whenGivenBalanceMessage_andUserExists_thenOk() {
        BalanceMessage balanceMessage = new BalanceMessage(100L, 200L, "", false);
        Account account = new Account(100L, 200L, "BOB", new BigDecimal(2000));

        when(accountRepository.findById(balanceMessage.getAccountNumber())).thenReturn(Optional.of(account));

        persistenceService.validateBalanceMessage(balanceMessage);

        assertFalse(balanceMessage.getErrors());
        assertThat(balanceMessage.getBalance()).isEqualTo(String.valueOf(account.getAccountBalance()));

    }

    @Test
    void whenGivenBalanceMessage_andUserDoesNotExist_thenErrorsIsTrue_Test() {
        BalanceMessage balanceMessage = new BalanceMessage(24L, 200L, "", false);
        when(accountRepository.findById(balanceMessage.getAccountNumber())).thenReturn(Optional.empty());
        persistenceService.validateBalanceMessage(balanceMessage);

        assertThat(balanceMessage.getErrors()).isTrue();
    }



    @Test
    void test_validateAndProcessTransferMessage_whenInvalidAmount_throwsInvalidTransferMsgException() {
        Creditor creditor = new Creditor(123, 123456);
        Debtor debtor = new Debtor(24, 234354);
        TransferMessage transferMessage = new TransferMessage(12, creditor, debtor, LocalDate.now(), BigDecimal.ZERO, "Memo message");

        assertThrows(InvalidTransferMessageException.class, () -> persistenceService.validateAndProcessTransferMessage(transferMessage));
    }

    @Test
    void test_validateAndProcessTransferMessage_onInvalidParties_throwsInvalidTransferMsgException() {
        Creditor creditor = new Creditor(123, 123456);
        Debtor debtor = new Debtor(24, 234354);
        TransferMessage transferMessage = new TransferMessage(12, creditor, debtor, LocalDate.now(), new BigDecimal(30), "Memo message");

        when(accountRepository.findById(any())).thenAnswer(invocationOnMock -> Optional.empty());

        assertThrows(InvalidTransferMessageException.class, () -> persistenceService.validateAndProcessTransferMessage(transferMessage));
    }

    @Test
    void test_validateAndProcessTransferMessage_debtorCannotMakePayment_throwsInvalidTransferMsgException() {
        Creditor creditor = new Creditor(123, 123456);
        Debtor debtor = new Debtor(24, 234354);
        TransferMessage transferMessage = new TransferMessage(12, creditor, debtor, LocalDate.now(), new BigDecimal(30), "Memo message");

        Account debtorAccount = new Account(debtor.getAccountNumber(), debtor.getRoutingNumber(), "BOB", new BigDecimal(9));
        Account creditorAccount = new Account(creditor.getAccountNumber(), creditor.getRoutingNumber(), "SPENCER", new BigDecimal(100));

        when(accountRepository.findById(debtor.getAccountNumber())).thenAnswer(invocationOnMock -> Optional.of(debtorAccount));

        when(accountRepository.findById(creditor.getAccountNumber())).thenAnswer(invocationOnMock -> Optional.of(creditorAccount));


        assertThrows(InvalidTransferMessageException.class, () -> persistenceService.validateAndProcessTransferMessage(transferMessage));
    }

    @Test
    void test_validateAndProcessTransferMessage_onValidTransferMessage_shouldUpdateBalanceAndInsertTransaction() {
        // given
        Creditor creditor = new Creditor(5, 55555);
        Debtor debtor = new Debtor(3,3333);
        Account creditorAccount = new Account(creditor.getAccountNumber(), creditor.getRoutingNumber(), "CREDITOR",new BigDecimal(100));
        Account debtorAccount = new Account(debtor.getAccountNumber(), debtor.getRoutingNumber(), "DEBTOR", new BigDecimal(100));
        TransferMessage transferMessage = new TransferMessage(12, creditor,debtor, LocalDate.now(), new BigDecimal(5), "For lunch");

        // when
        when(accountRepository.findById(debtor.getAccountNumber())).thenAnswer(invocationOnMock -> Optional.of(debtorAccount));

        when(accountRepository.findById(creditor.getAccountNumber())).thenAnswer(invocationOnMock -> Optional.of(creditorAccount));

        // then
        assertDoesNotThrow(() -> persistenceService.validateAndProcessTransferMessage(transferMessage));
    }


    @Test
    void test_enterTransaction_AND_enterTwoBankTransaction_whenTransactionAlreadyExists_willThrowInvalidTransferMessageException() {
        //given
        long transactionId = 2000;
        Creditor creditor = new Creditor(987, 111);
        Debtor debtor = new Debtor(876, 222);
        Transaction transaction = new Transaction(transactionId, creditor.getAccountNumber(), debtor.getAccountNumber(), BigDecimal.TEN, LocalDate.now(), "a memo for a transfer already initiated");
        PaymentParty paymentParty = new PaymentParty(new Account(debtor.getAccountNumber(), debtor.getRoutingNumber()),new Account(creditor.getAccountNumber(), creditor.getRoutingNumber()) );
        final TransferMessage alreadyPersistedTransferMessage = new TransferMessage(transactionId, creditor, debtor,  LocalDate.now(), BigDecimal.TEN, "memo");

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertThrows(InvalidTransferMessageException.class, () -> persistenceService.enterTransaction(alreadyPersistedTransferMessage, paymentParty));
        assertThrows(InvalidTransferMessageException.class, () -> persistenceService.enterTwoBankTransaction(alreadyPersistedTransferMessage));
    }





}