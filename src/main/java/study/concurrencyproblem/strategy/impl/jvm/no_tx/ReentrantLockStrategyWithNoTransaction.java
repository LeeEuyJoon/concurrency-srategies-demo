package study.concurrencyproblem.strategy.impl.jvm.no_tx;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.experiment.metrics.MetricContext;
import study.concurrencyproblem.repository.AccountRepository;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class ReentrantLockStrategyWithNoTransaction implements LockStrategy {

	private final LockMetrics metrics;
	private final AccountRepository accountRepository;
	private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

	public ReentrantLockStrategyWithNoTransaction(LockMetrics metrics, AccountRepository accountRepository) {
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
	public Strategy getStrategyType() { return REENTRANT_LOCK_WITH_NO_TX; }

	private Integer executeWithLock(Long id, ExperimentType experimentType
		, Supplier<Integer> criticalSection) {
		Strategy strategy = getStrategyType();
		ReentrantLock lock = locks.computeIfAbsent(id, k -> new ReentrantLock());

		MetricContext.set(strategy.name(), experimentType.name());
		try {
			long t0 = System.nanoTime();
			lock.lock();
			try {
				long waited = System.nanoTime() - t0;
				metrics.recordWait(strategy, experimentType, waited);
				return criticalSection.get();
			} finally {
				lock.unlock();
			}
		} finally {
			MetricContext.clear();
		}
	}
}
