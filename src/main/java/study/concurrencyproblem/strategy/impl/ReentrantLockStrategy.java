package study.concurrencyproblem.strategy.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.repository.AccountRepository;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;
import study.concurrencyproblem.experiment.ExperimentType;

@Component
public class ReentrantLockStrategy implements LockStrategy {

	private final LockMetrics metrics;
	private final AccountRepository accountRepository;
	private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

	public ReentrantLockStrategy(LockMetrics metrics, AccountRepository accountRepository) {
		this.metrics = metrics;
		this.accountRepository = accountRepository;
	}

	@Override
	public Integer getBalance(Long id, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () ->
				accountRepository.getBalance(id).orElseThrow()
			);
	}

	@Override
	public Integer withdraw(Long id, Integer amount, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () -> {
			Account account = accountRepository.findById(id).orElseThrow();
			account.setBalance(account.getBalance() - amount);
			accountRepository.save(account);
			return account.getBalance();
		});
	}

	@Override
	public Integer deposit(Long id, Integer amount, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () -> {
			Account account = accountRepository.findById(id).orElseThrow();
			account.setBalance(account.getBalance() + amount);
			accountRepository.save(account);
			return account.getBalance();
		});
	}

	@Override
	public Strategy getStrategyType() {
		return Strategy.REENTRANT_LOCK;
	}

	private Integer executeWithLock(Long id, ExperimentType experimentType
								, Supplier<Integer> criticalSection) {
		Strategy strategy = getStrategyType();
		ReentrantLock lock = locks.computeIfAbsent(id, k -> new ReentrantLock());

		long t0 = System.nanoTime();
		lock.lock();
		try {
			long waited = System.nanoTime() - t0;
			metrics.recordWait(strategy, experimentType, waited);
			return criticalSection.get();
		} finally {
			lock.unlock();
		}
	}
}
