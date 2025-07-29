package study.concurrencyproblem.core.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "balance", nullable = false)
    private Integer balance = 0;

    public Account() {}

    public Account(Integer balance) {
       this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }
    
    @Override
    public String toString() {
        return "Account{id=" + id + ", balance=" + balance + "}";
    }
} 