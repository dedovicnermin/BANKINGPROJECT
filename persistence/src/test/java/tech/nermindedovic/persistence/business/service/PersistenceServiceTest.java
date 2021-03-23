package tech.nermindedovic.persistence.business.service;

import joptsimple.internal.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.repository.AccountRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistenceServiceTest {

    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    private PersistenceService persistenceService;

    @Test
    void whenGivenBalanceMessage_andUserExists_thenOk() {
        BalanceMessage balanceMessage = createBalanceMsg(100L, 200L, "", false);
        Account account = createAccount(100L, 200L, "BOB", 2000);

        when(accountRepository.findById(balanceMessage.getAccountNumber())).thenReturn(Optional.of(account));

        persistenceService.validateBalanceMessage(balanceMessage);

        assertFalse(balanceMessage.getErrors());
        assertThat(balanceMessage.getBalance()).isEqualTo(String.format("%.2f", account.getAccountBalance()));

    }

    @Test
    void whenGivenBalanceMessage_andUserDoesNotExist_thenErrorsIsTrue_Test() {
        BalanceMessage balanceMessage = createBalanceMsg(24L, 200L, "", false);
        when(accountRepository.findById(balanceMessage.getAccountNumber())).thenReturn(Optional.empty());
        persistenceService.validateBalanceMessage(balanceMessage);

        assertThat(balanceMessage.getErrors()).isTrue();
    }


    private Account createAccount(long accountId, long routingNum, String userName, double balance) {
        return new Account(accountId, routingNum, userName, balance);
    }

    private BalanceMessage createBalanceMsg(long accountNumber, long routingNum, String balance, boolean errors) {
        return new BalanceMessage(accountNumber, routingNum, balance, errors);
    }

}