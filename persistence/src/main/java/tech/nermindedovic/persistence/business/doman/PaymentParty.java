package tech.nermindedovic.persistence.business.doman;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.nermindedovic.persistence.data.entity.Account;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentParty {

    Account debtorAccount;
    Account creditorAccount;

}
