package study.concurrencyproblem.strategy.impl.jvm.tx;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.experiment.metrics.MetricContext;
import study.concurrencyproblem.repository.AccountRepository;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;
import study.concurrencyproblem.experiment.ExperimentType;

@Component
public class ReentrantReadWriteLockStrategy implements LockStrategy {

	private final LockMetrics metrics;
	private final AccountRepository accountRepository;
	private final ConcurrentHashMap<Long, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

	public ReentrantReadWriteLockStrategy(LockMetrics metrics, AccountRepository accountRepository) {
		this.metrics = metrics;
		this.accountRepository = accountRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Integer getBalance(Long id, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () ->
			accountRepository.getBalance(id).orElseThrow()
			, false
		);
	}

	@Override
	@Transactional
	public Integer withdraw(Long id, Integer amount, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () -> {
			Account account = accountRepository.findById(id).orElseThrow();
			account.setBalance(account.getBalance() - amount);
			accountRepository.save(account);
			return account.getBalance();
		}, true);
	}

	@Override
	@Transactional
	public Integer deposit(Long id, Integer amount, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () -> {
			Account account = accountRepository.findById(id).orElseThrow();
			account.setBalance(account.getBalance() + amount);
			accountRepository.save(account);
			return account.getBalance();
		}, true);
	}

	@Override
	public Strategy getStrategyType() {
		return REENTRANT_READ_WRITE_LOCK;
	}

	private Integer executeWithLock(Long id, ExperimentType experimentType
									, Supplier<Integer> criticalSection, boolean isWrite) {
		Strategy strategy = getStrategyType();
		ReentrantReadWriteLock lock = locks.computeIfAbsent(id, k -> new ReentrantReadWriteLock());

		MetricContext.set(strategy.name(), experimentType.name());
		try {
			long t0 = System.nanoTime();

			if (isWrite) {
				lock.writeLock().lock();
			} else {
				lock.readLock().lock();
			}

			try {
				long waited = System.nanoTime() - t0;
				metrics.recordWait(strategy, experimentType, waited);
				return criticalSection.get();
			} finally {
				if (isWrite) {
					lock.writeLock().unlock();
				} else {
					lock.readLock().unlock();
				}
			}
		} finally {
			MetricContext.clear();
		}
	}
}