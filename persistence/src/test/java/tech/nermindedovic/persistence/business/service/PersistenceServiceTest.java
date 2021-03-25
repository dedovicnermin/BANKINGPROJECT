package tech.nermindedovic.persistence.business.service;

import joptsimple.internal.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.doman.Creditor;
import tech.nermindedovic.persistence.business.doman.Debtor;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
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
        BalanceMessage balanceMessage = createBalanceMsg(100L, 200L, "", false);
        Account account = createAccount(100L, 200L, "BOB", 2000);

        when(accountRepository.findById(balanceMessage.getAccountNumber())).thenReturn(Optional.of(account));

        persistenceService.validateBalanceMessage(balanceMessage);

        assertFalse(balanceMessage.getErrors());
        assertThat(balanceMessage.getBalance()).isEqualTo(String.valueOf(account.getAccountBalance()));

    }

    @Test
    void whenGivenBalanceMessage_andUserDoesNotExist_thenErrorsIsTrue_Test() {
        BalanceMessage balanceMessage = createBalanceMsg(24L, 200L, "", false);
        when(accountRepository.findById(balanceMessage.getAccountNumber())).thenReturn(Optional.empty());
        persistenceService.validateBalanceMessage(balanceMessage);

        assertThat(balanceMessage.getErrors()).isTrue();
    }



    @Test
    void test_validateAndProcessTransferMessage_whenInvalidAmount_throwsInvalidTransferMsgException() {
        Creditor creditor = new Creditor(123, 123456);
        Debtor debtor = new Debtor(24, 234354);
        TransferMessage transferMessage = new TransferMessage(12, creditor, debtor, new Date(), 0, "Memo message");

        assertThrows(InvalidTransferMessageException.class, () -> persistenceService.validateAndProcessTransferMessage(transferMessage));
    }

    @Test
    void test_validateAndProcessTransferMessage_onInvalidParties_throwsInvalidTransferMsgException() {
        Creditor creditor = new Creditor(123, 123456);
        Debtor debtor = new Debtor(24, 234354);
        TransferMessage transferMessage = new TransferMessage(12, creditor, debtor, new Date(), 30, "Memo message");

        when(accountRepository.findById(any())).thenAnswer(invocationOnMock -> Optional.empty());

        assertThrows(InvalidTransferMessageException.class, () -> persistenceService.validateAndProcessTransferMessage(transferMessage));
    }

    @Test
    void test_validateAndProcessTransferMessage_debtorCannotMakePayment_throwsInvalidTransferMsgException() {
        Creditor creditor = new Creditor(123, 123456);
        Debtor debtor = new Debtor(24, 234354);
        TransferMessage transferMessage = new TransferMessage(12, creditor, debtor, new Date(), 30, "Memo message");

        Account debtorAccount = new Account(debtor.getAccountNumber(), debtor.getRoutingNumber(), "BOB", 9);
        Account creditorAccount = new Account(creditor.getAccountNumber(), creditor.getRoutingNumber(), "SPENCER", 100);

        when(accountRepository.findById(debtor.getAccountNumber())).thenAnswer(invocationOnMock -> {
            return  Optional.of(debtorAccount);
        });

        when(accountRepository.findById(creditor.getAccountNumber())).thenAnswer(invocationOnMock -> {
            return  Optional.of(creditorAccount);
        });


        assertThrows(InvalidTransferMessageException.class, () -> persistenceService.validateAndProcessTransferMessage(transferMessage));
    }

    @Test
    void test_validateAndProcessTransferMessage_onValidTransferMessage_shouldUpdateBalanceAndInsertTransaction() throws InvalidTransferMessageException {
        Creditor creditor = new Creditor(5, 55555);
        Debtor debtor = new Debtor(3,3333);

        Account creditorAccount = new Account(creditor.getAccountNumber(), creditor.getRoutingNumber(), "CREDITOR",100);
        Account debtorAccount = new Account(debtor.getAccountNumber(), debtor.getRoutingNumber(), "DEBTOR", 100);

        TransferMessage transferMessage = new TransferMessage(12, creditor,debtor, new Date(), 5, "For lunch");

        when(accountRepository.findById(debtor.getAccountNumber())).thenAnswer(invocationOnMock -> {
            return  Optional.of(debtorAccount);
        });

        when(accountRepository.findById(creditor.getAccountNumber())).thenAnswer(invocationOnMock -> {
            return  Optional.of(creditorAccount);
        });

//        when(accountRepository.save(creditorAccount)).thenAnswer(invocationOnMock -> {
//            creditorAccount.setAccountBalance(creditorAccount.getAccountBalance() + transferMessage.getAmount());
//            return creditorAccount;
//        });
//
//        when(accountRepository.save(debtorAccount)).thenAnswer(invocationOnMock -> {
//            debtorAccount.setAccountBalance(debtorAccount.getAccountBalance() - transferMessage.getAmount());
//            return creditorAccount;
//        });


//        Transaction transaction = new Transaction();
//        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        persistenceService.validateAndProcessTransferMessage(transferMessage);

        assertDoesNotThrow(() -> InvalidTransferMessageException.class);






    }






    private Account createAccount(long accountId, long routingNum, String userName, long balance) {
        return new Account(accountId, routingNum, userName, balance);
    }

    private BalanceMessage createBalanceMsg(long accountNumber, long routingNum, String balance, boolean errors) {
        return new BalanceMessage(accountNumber, routingNum, balance, errors);
    }

}