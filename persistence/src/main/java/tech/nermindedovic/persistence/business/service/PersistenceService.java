package tech.nermindedovic.persistence.business.service;

import com.sun.istack.NotNull;
import org.springframework.stereotype.Service;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.business.doman.TransferMessage;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.entity.Transaction;
import tech.nermindedovic.persistence.data.repository.AccountRepository;
import tech.nermindedovic.persistence.data.repository.TransactionRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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






    /**
     * picks up from xmlProcessor.
     * PRECONDITION: xml must have successfully binded.
     * POSTCONDITION: will return true if successful or else false
     * @param
     *
     *
     */
    public boolean validateTransferMessage(@NotNull final TransferMessage transferMessage) {
        Map<String, Long> validUsers = usersFound(transferMessage);

        if (validUsers.isEmpty() || errorsPresent(transferMessage)) {
            return false;
        }
        createAndPersistRecords(validUsers, transferMessage);
        return true;

    }


    /**
     * Returns K:V pairs for valid account.
     * Keys = Debtor or Creditor
     * Value = accountNumber
     *
     * Done in order to know if the one user found is a creditor or a debtor
     * @param transferMessage
     * @return
     */
    private Map<String, Long> usersFound(final TransferMessage transferMessage) {
        Map<String, Long> map = new HashMap<>();

        accountRepository.findById(transferMessage.getDebtor().getAccountNumber())
                .ifPresent(account -> map.put("Debtor", account.getAccountNumber()));


        accountRepository.findById(transferMessage.getCreditor().getAccountNumber())
                .ifPresent(account -> map.put("Creditor", account.getAccountNumber()));


        return Collections.unmodifiableMap(map);

    }


    /**
     * Will go through entries (max = 2)
     * @param validUsers
     * @param transferMessage
     */
    private void createAndPersistRecords(Map<String, Long> validUsers, final TransferMessage transferMessage) {
        for (Map.Entry<String, Long> entry : validUsers.entrySet()) {
            if (entry.getKey() == "Debtor")
                createAndPersistDebtorRecord(entry.getValue(), transferMessage);
            else
                createAndPersistCreditorRecord(entry.getValue(), transferMessage);
        }
    }










    /**
     * PRECONDITION: Both creditor and debtor are in our db. Creditor cannot update balance with value <=0.
     * POSTCONDITION: new tranaction will persist for creditor, balance on account updated.
     * @param accountNumber
     * @param balanceMessage
     * @return
     */

    private Optional<Transaction> createAndPersistDebtorRecord(long accountNumber, TransferMessage balanceMessage) {

    }

    private Optional<Transaction> createAndPersistCreditorRecord(long accountNumber, TransferMessage balanceMessage) {

    }




    /**
     *
     * PRECONDITION : user is valid. -> based on int ret by usersFound()
     *
     * BALANCE IN DB IS DOUBLE. NEED TO CHANGE.
     * @param accountNumber
     * @return
     */
    private double retrieveBalanceFromValidUser(long accountNumber) {
        Account validAccount = accountRepository.findById(accountNumber).get();
        return validAccount.getAccountBalance();
    }

    private void recordTransaction(Transaction transaction) {

    }






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



}
