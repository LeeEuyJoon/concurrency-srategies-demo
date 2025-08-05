package study.concurrencyproblem.strategy.impl;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class NoLockStrategy implements LockStrategy {
    
    @Override
    public Integer getBalance(Long id) {
        // TODO
		return 0;
    }

	@Override
	public Integer withdraw(Long id, Integer amount) {
		// TODO
		return 0;
	}

	@Override
	public Integer deposit(Long id, Integer amount) {
		// TODO
		return 0;
	}

	@Override
	public Strategy getStrategyType() {
		return Strategy.NO_LOCK;
	}
}
