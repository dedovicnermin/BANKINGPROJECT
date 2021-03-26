package tech.nermindedovic.persistence.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import tech.nermindedovic.persistence.data.utils.TransactionAttributes;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Generated
@AllArgsConstructor
@NoArgsConstructor
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
    private BigDecimal amount;

    @Column(name = TransactionAttributes.DATE)
    private Date date;

    @Column(name = TransactionAttributes.MEMO)
    private String memo;




}
