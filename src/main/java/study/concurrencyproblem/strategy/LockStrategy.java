package study.concurrencyproblem.strategy;

import study.concurrencyproblem.experiment.ExperimentType;

public interface LockStrategy {
    
    Integer getBalance(Long id, ExperimentType experimentType);

    Integer withdraw(Long id, Integer amount, ExperimentType experimentType);

    Integer deposit(Long id, Integer amount, ExperimentType experimentType);

    Strategy getStrategyType();


}
