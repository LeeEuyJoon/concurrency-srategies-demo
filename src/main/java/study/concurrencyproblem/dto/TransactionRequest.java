package study.concurrencyproblem.dto;

import study.concurrencyproblem.strategy.Strategy;
import study.experiment.ExperimentType;

public class TransactionRequest {
    private Integer amount;
    private Strategy strategy = Strategy.NO_LOCK;
    private ExperimentType experimentType = ExperimentType.WITHDRAW_ONLY;

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

    public ExperimentType getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(ExperimentType experimentType) {
        this.experimentType = experimentType;
    }
}