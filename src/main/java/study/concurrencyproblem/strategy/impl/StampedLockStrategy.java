package study.concurrencyproblem.strategy.impl;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;
import study.experiment.ExperimentType;

@Component
public class StampedLockStrategy implements LockStrategy {

	@Override
	public Integer getBalance(Long id, ExperimentType experimentType) {
		// TODO
		return 0;
	}

	@Override
	public Integer withdraw(Long id, Integer amount, ExperimentType experimentType) {
		// TODO
		return 0;
	}

	@Override
	public Integer deposit(Long id, Integer amount, ExperimentType experimentType) {
		// TODO
		return 0;
	}

	@Override
	public Strategy getStrategyType() {
		return Strategy.STAMPED_LOCK;
	}
}
