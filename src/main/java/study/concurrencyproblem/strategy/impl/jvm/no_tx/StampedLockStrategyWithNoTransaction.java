package study.concurrencyproblem.strategy.impl.jvm.no_tx;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.repository.AccountRepository;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class StampedLockStrategyWithNoTransaction implements LockStrategy {

	private final LockMetrics metrics;
	private final AccountRepository accountRepository;
	private final ConcurrentHashMap<Long, StampedLock> locks = new ConcurrentHashMap<>();

	public StampedLockStrategyWithNoTransaction(LockMetrics metrics, AccountRepository accountRepository) {
		this.metrics = metrics;
		this.accountRepository = accountRepository;
	}

	@Override
	public Integer getBalance(Long id, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () ->
				accountRepository.getBalance(id).orElseThrow()
			, false);
	}

	@Override
	public Integer withdraw(Long id, Integer amount, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () -> {
			Account account = accountRepository.findById(id).orElseThrow();
			account.setBalance(account.getBalance() - amount);
			accountRepository.save(account);
			return account.getBalance();
		}, true);
	}

	@Override
	public Integer deposit(Long id, Integer amount, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () -> {
			Account account = accountRepository.findById(id).orElseThrow();
			account.setBalance(account.getBalance() + amount);
			accountRepository.save(account);
			return account.getBalance();
		}, true);
	}

	@Override
	public Strategy getStrategyType() { return STAMPED_LOCK_WITH_NO_TX; }

	private Integer executeWithLock(Long id, ExperimentType experimentType
		, Supplier<Integer> criticalSection, boolean isWrite) {
		Strategy strategy = getStrategyType();
		StampedLock lock = locks.computeIfAbsent(id, k -> new StampedLock());

		long t0 = System.nanoTime();

		if (isWrite) {
			long stamp = lock.writeLock();

			try {
				long waited = System.nanoTime() - t0;
				metrics.recordWait(strategy, experimentType, waited);
				return criticalSection.get();
			} finally {
				lock.unlockWrite(stamp);
			}
		} else {
			long stamp = lock.tryOptimisticRead();
			Integer result = criticalSection.get();

			if (!lock.validate(stamp)) {
				stamp = lock.readLock();
				try {
					long waited = System.nanoTime() - t0;
					metrics.recordWait(strategy, experimentType, waited);
					result = criticalSection.get();
				} finally {
					lock.unlockRead(stamp);
				}
			}
			return result;
		}
	}
}
