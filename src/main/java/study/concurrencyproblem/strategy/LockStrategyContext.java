package study.concurrencyproblem.strategy;

import org.springframework.stereotype.Component;
import study.concurrencyproblem.experiment.ExperimentType;

@Component
public class LockStrategyContext {
	private LockStrategy strategy;

	public void setStrategy(LockStrategy strategy) {
		this.strategy = strategy;
	}

	public Integer getBalance(Long id, ExperimentType experimentType) {
		return strategy.getBalance(id, experimentType);
	}
	public Integer withdraw(Long id, Integer amount, ExperimentType experimentType) {
		return strategy.withdraw(id, amount, experimentType);
	}
	public Integer deposit(Long id, Integer amount, ExperimentType experimentType) {
		return strategy.deposit(id, amount, experimentType);
	}
}

