package tech.nermindedovic.persistence.data.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import tech.nermindedovic.persistence.data.utils.AccountAttributes;

import javax.persistence.*;

@Entity
@Table(name = "ACCOUNTS")
@Data
@Generated
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = AccountAttributes.ACCT_NUM)
    private long accountNumber;

    @Column(name = AccountAttributes.ROUT_NUM)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long routingNumber;

    @Column(name = AccountAttributes.NAME)
    private String userName;

    @Column(name = AccountAttributes.BALANCE)
    private long accountBalance;


}
