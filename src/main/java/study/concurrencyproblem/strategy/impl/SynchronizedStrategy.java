package study.concurrencyproblem.strategy.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.repository.AccountRepository;
import study.concurrencyproblem.strategy.LockMetrics;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class SynchronizedStrategy implements LockStrategy {

	private final LockMetrics metrics;
	private final AccountRepository accountRepository;
	private final ConcurrentHashMap<String, Object> monitors = new ConcurrentHashMap<>();

	public SynchronizedStrategy(LockMetrics metrics, AccountRepository accountRepository) {
		this.metrics = metrics;
		this.accountRepository = accountRepository;
	}

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
		return Strategy.SYNCHRONIZED;
	}
}
