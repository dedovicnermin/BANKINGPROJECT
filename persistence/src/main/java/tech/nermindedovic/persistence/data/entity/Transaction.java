package tech.nermindedovic.persistence.data.entity;

import lombok.Data;
import tech.nermindedovic.persistence.data.utils.TransactionAttributes;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "TRANSACTIONS")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = TransactionAttributes.TRANSACTION_ID)
    private long transactionId;



    @Column(name = TransactionAttributes.CREDITOR)
    private long creditorAccountNumber;


    @Column(name = TransactionAttributes.DEBTOR)
    private long debtorAccountNumber;


    @Column(name = TransactionAttributes.AMOUNT)
    private long amount;

    @Column(name = TransactionAttributes.DATE)
    private Date date;

    @Column(name = TransactionAttributes.MEMO)
    private String memo;




}
