package tech.nermindedovic.persistence.data.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import tech.nermindedovic.persistence.data.utils.AccountAttributes;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ACCOUNTS")
@Data
@Generated
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @Column(name = AccountAttributes.ACCT_NUM)
    private long accountNumber;

    @Column(name = AccountAttributes.ROUT_NUM)
    private long routingNumber;

    @Column(name = AccountAttributes.NAME)
    private String userName;

    @Column(name = AccountAttributes.BALANCE)
    private BigDecimal accountBalance;

    public Account(long accountNumber, long routingNumber) {
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
    }



}
