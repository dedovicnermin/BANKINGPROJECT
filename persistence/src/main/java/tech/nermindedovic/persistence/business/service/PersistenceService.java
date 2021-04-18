package tech.nermindedovic.persistence.business.service;



import org.springframework.stereotype.Service;
import tech.nermindedovic.persistence.business.doman.*;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;
import tech.nermindedovic.persistence.exception.InvalidTransferMessageException;

import javax.validation.constraints.NotNull;
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
     *
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
     * POST-CONDITION : DB will have created ledger transaction records for both parties and update each balance for user.
     */
    public void validateAndProcessTransferMessage(@NotNull final TransferMessage transferMessage) throws InvalidTransferMessageException {
        PaymentParty paymentParty = validateTransferMessage(transferMessage);
        processTransferMessage(transferMessage, paymentParty);
    }


    /**
     * Validates input prior to processing.
     * Ensure data is accurate. Will be used to make changes to db.
     */
    private PaymentParty validateTransferMessage(final TransferMessage transferMessage) throws InvalidTransferMessageException {
        amountIsValid(transferMessage.getAmount());
        PaymentParty paymentParty = accountsAreValid(transferMessage);
        debtorCanTransferAmount(paymentParty, transferMessage.getAmount());
        return paymentParty;
    }





    /**
     * PRECONDITION:    both parties have been VALIDATED.
     * POST-CONDITION:   transaction record saved / balances updated
     *
     *
     */
    private void processTransferMessage(final TransferMessage transferMessage, final PaymentParty paymentParty) throws InvalidTransferMessageException {
        enterTransaction(transferMessage, paymentParty);
        updateBalance(paymentParty, transferMessage.getAmount());
    }




    /**
     * PRECONDITION:    transferMessage contains no errors
     * POST-CONDITION:   new transaction created/persisted
     *
     * @param transferMessage carrying valid transaction data
     */
    public void enterTransaction(final TransferMessage transferMessage, PaymentParty paymentParty) throws InvalidTransferMessageException {
        if (messageExists(transferMessage)) throw new InvalidTransferMessageException(String.format("Transaction with ID {%d} already exists.", transferMessage.getMessage_id()));

        Transaction transaction = new Transaction();
        transaction.setTransactionId(transferMessage.getMessage_id());
        setAccountNumbers(paymentParty, transaction);
        transaction.setAmount(transferMessage.getAmount());
        transaction.setDate(transferMessage.getDate());
        transaction.setMemo(transferMessage.getMemo());
        transactionRepository.save(transaction);
    }


    /**
     * PRECONDITION: paymentParty carries valid accounts. Debtor has enough to make this transfer
     * POST-CONDITION: paymentParty will have balances updated according to role in transfer
     *
     * @param paymentParty carrying debtor/creditor accounts
     * @param amount found in transferMessage
     */
    private void updateBalance(final PaymentParty paymentParty, final BigDecimal amount) {

        Account debtorAccount = paymentParty.getDebtorAccount();
        debtorAccount.setAccountBalance(getUpdatedDebtorBalance(debtorAccount.getAccountBalance(), amount));
        accountRepository.save(debtorAccount);

        Account creditorAccount = paymentParty.getCreditorAccount();
        creditorAccount.setAccountBalance(getUpdatedCreditorBalance(creditorAccount.getAccountBalance(), amount));
        accountRepository.save(creditorAccount);

    }


    /**
     * Ensures the transfer amount is valid
     * @throws InvalidTransferMessageException if amount is invalid
     */
    private void amountIsValid(BigDecimal amount) throws InvalidTransferMessageException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new InvalidTransferMessageException("Transfer Message contains an invalid payment amount.");
    }



    /**
     * PRECONDITION: transferMessage will be free of errors outside of validating parties
     * POST-CONDITION: will return true if both parties valid, else false
     * Ensures BOTH parties have a relationship with this bank.
     *
     * @return a paymentParty holding valid account information
     */
    private PaymentParty accountsAreValid(final TransferMessage transferMessage) throws InvalidTransferMessageException {
        Optional<Account> creditor = accountRepository.findById(transferMessage.getCreditor().getAccountNumber());
        Optional<Account> debtor = accountRepository.findById(transferMessage.getDebtor().getAccountNumber());

        if (!(creditor.isPresent() && debtor.isPresent())) {
            throw new InvalidTransferMessageException("Both accounts are not users of this bank.");
        }

        return new PaymentParty(debtor.get(), creditor.get());
    }



    /**
     * PRECONDITION: PaymentParty carries valid accounts. Amount is greater than 0
     * POST-CONDITION: exception will be thrown or processing will continue
     *
     * @param paymentParty debtor/creditor
     * @param amount from transferMessage
     * @throws InvalidTransferMessageException on insufficient funds
     */
    private void debtorCanTransferAmount(final PaymentParty paymentParty, BigDecimal amount) throws InvalidTransferMessageException {
        BigDecimal balance = paymentParty.getDebtorAccount().getAccountBalance();
        if (cannotMakePayment(balance,amount)) {
            throw new InvalidTransferMessageException("Debtor cannot make this payment with the current balance.");
        }
    }


    /**
     * Negative funds check
     */
    private boolean cannotMakePayment(final BigDecimal debtorBalance, final BigDecimal amount) {
        return (debtorBalance.subtract(amount)).compareTo(BigDecimal.ZERO) <= 0;
    }

    private void setAccountNumbers(final PaymentParty paymentParty, Transaction transaction) {
        transaction.setDebtorAccountNumber(paymentParty.getDebtorAccount().getAccountNumber());
        transaction.setCreditorAccountNumber(paymentParty.getCreditorAccount().getAccountNumber());
    }


    private BigDecimal getUpdatedDebtorBalance(final BigDecimal debtorAccountBalance, final BigDecimal amount) {
        return debtorAccountBalance.subtract(amount).setScale(2, RoundingMode.HALF_EVEN);
    }

    private BigDecimal getUpdatedCreditorBalance(final BigDecimal creditorAccountBalance, final BigDecimal amount) {
        return creditorAccountBalance.add(amount).setScale(2, RoundingMode.HALF_EVEN);
    }


    private boolean messageExists(final TransferMessage transferMessage) {
        return transactionRepository.findById(transferMessage.getMessage_id()).isPresent();
    }

}



