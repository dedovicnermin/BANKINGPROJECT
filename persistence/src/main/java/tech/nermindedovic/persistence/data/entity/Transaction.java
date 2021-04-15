package tech.nermindedovic.persistence.data.entity;

import lombok.*;
import tech.nermindedovic.persistence.data.utils.TransactionAttributes;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Data
@Generated
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "TRANSACTIONS")
public class Transaction {

    @Id
    @Column(name = TransactionAttributes.TRANSACTION_ID)
    private long transactionId;

    @Column(name = TransactionAttributes.CREDITOR)
    private long creditorAccountNumber;

    @Column(name = TransactionAttributes.DEBTOR)
    private long debtorAccountNumber;

    @Column(name = TransactionAttributes.AMOUNT)
    private BigDecimal amount;

    @Column(name = TransactionAttributes.DATE)
    private LocalDate date;

    @Column(name = TransactionAttributes.MEMO)
    private String memo;




}
