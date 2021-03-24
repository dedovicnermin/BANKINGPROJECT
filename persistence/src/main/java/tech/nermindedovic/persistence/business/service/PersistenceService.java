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
            balanceMessage.setBalance(String.format("%.2f", account1.getAccountBalance()));
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
    public void validateAndProcessTransferMessage(@NotNull final TransferMessage transferMessage) {

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
    private void validateTransferMessage(final TransferMessage transferMessage) throws Exception {

        if (errorsPresent(transferMessage) || areValidParties(transferMessage)) throw new Exception("Invalid Account(s) / Invalid msg. DENIED ");
        try {
            processTransferMessage(transferMessage);
        } catch (Exception e) {

        }

    }





    /**
     * PRECONDITION:    both parties have been VALIDATED.
     * POSTCONDITION:   transaction record has been created for both parties respectively. each party will have their balance updated
     * @param transferMessage
     * @return
     */
    private void processTransferMessage(final TransferMessage transferMessage) throws Exception {
        processDebtorParty(transferMessage);
        processCreditorParty(transferMessage);
    }






    private void processDebtorParty(final TransferMessage transferMessage) throws Exception {
        Account debtor = accountRepository.findById(transferMessage.getDebtor().getAccountNumber()).get();
        long prevBal = debtor.getAccountBalance();
        long newBalance = prevBal - transferMessage.getAmount();

        if (newBalance < 0) throw new Exception("Debtor will be in the negative upon making this transfer. DENIED.");

        Transaction transaction = new Transaction();
        transaction.setAccountNumber(debtor.getAccountNumber());
        transaction.setPartyAccountNumber(transferMessage.getCreditor().getAccountNumber());
        transaction.setAmount(transferMessage.getAmount());
        transaction.setDate(transferMessage.getDate());
        transaction.setMemo(transferMessage.getMemo());
        transaction.setTransactionType('D');

        transaction.setPreviousBalance(prevBal);
        transaction.setNewBalance(newBalance);

        debtor.setAccountBalance(newBalance);
        accountRepository.save(debtor);

        transactionRepository.save(transaction);
    }








    private void processCreditorParty(final TransferMessage transferMessage) {
        Account creditor = accountRepository.findById(transferMessage.getCreditor().getAccountNumber()).get();
        long prevBal = creditor.getAccountBalance();
        long newBal = prevBal + transferMessage.getAmount();

        Transaction transaction = new Transaction();
        transaction.setAccountNumber(creditor.getAccountNumber());
        transaction.setPartyAccountNumber(transferMessage.getDebtor().getAccountNumber());
        transaction.setAmount(transferMessage.getAmount());
        transaction.setDate(transferMessage.getDate());
        transaction.setMemo(transaction.getMemo());
        transaction.setTransactionType('C');

        transaction.setPreviousBalance(prevBal);
        transaction.setNewBalance(newBal);

        creditor.setAccountBalance(newBal);
        accountRepository.save(creditor);

        transactionRepository.save(transaction);

    }











//        accountRepository.findById(transferMessage.getDebtor().getAccountNumber())
//                .ifPresent(account -> map.put("Debtor", account.getAccountNumber()));
//
//
//        accountRepository.findById(transferMessage.getCreditor().getAccountNumber())
//                .ifPresent(account -> map.put("Creditor", account.getAccountNumber()));


















    /**
     * Function suited for any simple validation to message prior to doing any communication with database.
     *
     * AMOUNT SHOULD BE POSITIVE
     * @param transferMessage
     * @return
     */
    private boolean errorsPresent(final TransferMessage transferMessage) {
        return transferMessage.getAmount() <= 0;
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
    private boolean areValidParties(final TransferMessage transferMessage) {
        return accountRepository.findById(transferMessage.getCreditor().getAccountNumber()).isPresent() &&
                accountRepository.findById(transferMessage.getDebtor().getAccountNumber()).isPresent();
    }



}
