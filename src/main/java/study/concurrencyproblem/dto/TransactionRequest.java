package study.concurrencyproblem.dto;

import study.concurrencyproblem.strategy.Strategy;

public class TransactionRequest {
    private Integer amount;
    private Strategy strategy = Strategy.NO_LOCK;
    
    // 기본 생성자
    public TransactionRequest() {}
    
    public TransactionRequest(Integer amount) {
        this.amount = amount;
    }

    public TransactionRequest(Integer amount, Strategy strategy) {
        this.amount = amount;
        this.strategy = strategy;
    }

    public Integer getAmount() {
        return amount;
    }
    
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    
    public Strategy getStrategy() {
        return strategy;
    }
    
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
}