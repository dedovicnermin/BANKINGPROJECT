package tech.nermindedovic.persistence.data.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import tech.nermindedovic.persistence.data.utils.AccountAttributes;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ACCOUNTS")
@Data
@Generated
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

    public Account(long accountNumber, long routingNumber, String userName, BigDecimal amount) {
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
        this.userName = userName;
        this.accountBalance = amount;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }



}
