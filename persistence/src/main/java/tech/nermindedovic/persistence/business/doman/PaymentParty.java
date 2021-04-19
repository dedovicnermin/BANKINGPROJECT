package tech.nermindedovic.persistence.business.doman;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tech.nermindedovic.persistence.data.entity.Account;

@Getter
@AllArgsConstructor
public class PaymentParty {

    private Account debtorAccount;
    private Account creditorAccount;

}
