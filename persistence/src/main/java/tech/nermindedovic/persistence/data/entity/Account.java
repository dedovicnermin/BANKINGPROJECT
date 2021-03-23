package tech.nermindedovic.persistence.data.entity;


import lombok.Data;
import tech.nermindedovic.persistence.data.utils.AccountAttributes;

import javax.persistence.*;

@Entity
@Table(name = "ACCOUNTS")
@Data
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = AccountAttributes.ACCT_NUM)
    private long accountNumber;

    @Column(name = AccountAttributes.ROUT_NUM)
    private long routingNumber;

    @Column(name = AccountAttributes.NAME)
    private String userName;

    @Column(name = AccountAttributes.BALANCE)
    private double accountBalance;


}
