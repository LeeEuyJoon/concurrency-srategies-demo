package study.concurrencyproblem.strategy.impl;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.strategy.LockStrategy;

@Component
public class StampedLockStrategy implements LockStrategy {
	@Override
	public Integer getBalance(Long id) {
		return 0;
	}

	@Override
	public Integer withdraw(Long id, Integer amount) {
		return 0;
	}

	@Override
	public Integer deposit(Long id, Integer amount) {
		return 0;
	}
}
