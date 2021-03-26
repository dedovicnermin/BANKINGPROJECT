package tech.nermindedovic.persistence.business.service;

import com.sun.istack.NotNull;
import org.springframework.stereotype.Service;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.doman.Creditor;
import tech.nermindedovic.persistence.business.doman.Debtor;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class PersistenceService {

    /**
     * == dependency ==
     * to access Data
     */
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;



    // == constructor ==
    public PersistenceService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }



    // == BALANCE UPDATE ==


    /**
     * @param balanceMessage POJO of XML balance request message
     * @return  void
     *
     * We are here bc the BalanceRequest has made it through the bind phase, indicating valid msg format
     * last phase of processing, leaf node.
     *
     */
    public void validateBalanceMessage(@NotNull final BalanceMessage balanceMessage) {
        Optional<Account> account = accountRepository.findById(balanceMessage.getAccountNumber());

        balanceMessage.setErrors(true);

        account.ifPresent(account1 -> {
            balanceMessage.setBalance(account1.getAccountBalance().toString());
            balanceMessage.setErrors(false);
        });

    }





    // == FUNDS TRANSFER ==


    /**
     * STARTING POINT FOR TRANSFER FUNDS.
     *
     * PRECONDITION  : XML processing service should have successfully been able to bind the xml message into a transferMessage
     * POSTCONDITION : DB will have created ledger transaction records for both parties and update each balance for user.
     *
     *
     *
     */
    public void validateAndProcessTransferMessage(@NotNull final TransferMessage transferMessage) throws InvalidTransferMessageException {
        validateTransferMessage(transferMessage);
        processTransferMessage(transferMessage);
    }


    /**
     * Validates input prior to processing.
     * Ensure data is accurate. Will be used to make changes to db.
     * @param transferMessage
     * @return
     *
     */
    private void validateTransferMessage(final TransferMessage transferMessage) throws InvalidTransferMessageException {
        amountIsValid(transferMessage.getAmount());
        accountsAreValid(transferMessage);
        debtorCanTransferAmount(transferMessage.getDebtor(), transferMessage.getAmount());
    }







    /**
     * PRECONDITION:    both parties have been VALIDATED.
     * POSTCONDITION:   transaction record saved / balances updated
     * @param transferMessage
     * @return
     */
    private void processTransferMessage(final TransferMessage transferMessage) {
        updateBalance(transferMessage.getDebtor(), transferMessage.getCreditor(), transferMessage.getAmount());
        enterTransaction(transferMessage);
    }


    /**
     * PRECONDITION:    transferMessage contains no errors
     * POSTCONDITION:   new transaction created/persisted
     * @param transferMessage
     */
    private void enterTransaction(final TransferMessage transferMessage) {
        Transaction transaction = new Transaction();

        setAccountNumbers(transferMessage.getDebtor(), transferMessage.getCreditor(), transaction);
        transaction.setAmount(transferMessage.getAmount());
        transaction.setDate(transferMessage.getDate());
        transaction.setMemo(transferMessage.getMemo());

        transactionRepository.save(transaction);
    }


    /**
     * PRECONDITION: debtor/creditor are valid accounts. Debtor has enough to make this transfer
     * POSTCONDITION: debtor/creditor will have balances updated according to role in transfer
     * @param debtor
     * @param creditor
     * @param amount
     */
    private void updateBalance(final Debtor debtor, final Creditor creditor,final BigDecimal amount) {
        Account debtorAccount = accountRepository.findById(debtor.getAccountNumber()).get();
        debtorAccount.setAccountBalance(debtorAccount.getAccountBalance().subtract(amount).setScale(2, RoundingMode.HALF_EVEN));
        accountRepository.save(debtorAccount);

        Account creditorAccount = accountRepository.findById(creditor.getAccountNumber()).get();
        creditorAccount.setAccountBalance(creditorAccount.getAccountBalance().add(amount).setScale(2, RoundingMode.HALF_EVEN));
        accountRepository.save(creditorAccount);

    }


    /**
     * Ensures the transfer amount is valid
     * @param amount
     * @throws InvalidTransferMessageException
     */
    private void amountIsValid(BigDecimal amount) throws InvalidTransferMessageException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new InvalidTransferMessageException("Transfer Message contains an invalid payment amount.");
    }







    /**
     * PRECONDITION: transferMessage will be free of errors outside of validating parties
     * POSTCONDITION: will return true if both parties valid, else false
     * Ensures BOTH parties have a relationship with this bank.
     *
     *
     * @param transferMessage
     * @return
     */
    private void accountsAreValid(final TransferMessage transferMessage) throws InvalidTransferMessageException {
        if (!(accountRepository.findById(transferMessage.getCreditor().getAccountNumber()).isPresent() &&
                accountRepository.findById(transferMessage.getDebtor().getAccountNumber()).isPresent())) {
            throw new InvalidTransferMessageException("Both accounts could not be validated");
        }
    }


    /**
     * PRECONDITION: debtor and creditor have valid acocunts. Amount is greater than 0
     * POSTCONDITION: exception will be thrown or processing will continue
     * @param debtor
     * @param amount
     * @throws InvalidTransferMessageException
     */
    private void debtorCanTransferAmount(final Debtor debtor, BigDecimal amount) throws InvalidTransferMessageException {
        BigDecimal balance = accountRepository.findById(debtor.getAccountNumber()).get().getAccountBalance();
        if (cannotMakePayment(balance,amount)) {
            throw new InvalidTransferMessageException("Debtor cannot make this payment with the current balance.");
        }

    }

    /**
     * Negative funds check
     * @param debtorBalance
     * @param amount
     * @return
     */
    private boolean cannotMakePayment(final BigDecimal debtorBalance,final BigDecimal amount) {
        return (debtorBalance.subtract(amount)).compareTo(BigDecimal.ZERO) <= 0;
    }

    private void setAccountNumbers(final Debtor debtor, final Creditor creditor, Transaction transaction) {
        transaction.setDebtorAccountNumber(debtor.getAccountNumber());
        transaction.setCreditorAccountNumber(creditor.getAccountNumber());
    }

}



//        accountRepository.findById(transferMessage.getDebtor().getAccountNumber())
//                .ifPresent(account -> map.put("Debtor", account.getAccountNumber()));
//
//
//        accountRepository.findById(transferMessage.getCreditor().getAccountNumber())
//                .ifPresent(account -> map.put("Creditor", account.getAccountNumber()));

