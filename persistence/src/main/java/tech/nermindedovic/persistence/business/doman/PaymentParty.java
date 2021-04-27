package tech.nermindedovic.persistence.business.doman;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.nermindedovic.persistence.data.entity.Account;

@Getter
@AllArgsConstructor
public class PaymentParty {

    private final Account debtorAccount;
    private final Account creditorAccount;

}
