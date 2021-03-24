package tech.nermindedovic.persistence.business.service;

import com.sun.istack.NotNull;
import org.springframework.stereotype.Service;
import tech.nermindedovic.persistence.business.doman.BalanceMessage;
import tech.nermindedovic.persistence.data.entity.Account;
import tech.nermindedovic.persistence.data.repository.AccountRepository;

import java.util.Optional;

@Service
public class PersistenceService {

    /**
     * == dependency ==
     * to access DTO
     */
    private final AccountRepository accountRepository;




    // == constructor ==
    public PersistenceService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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



}
