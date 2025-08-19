package study.concurrencyproblem.strategy.impl.jvm.no_tx;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.experiment.metrics.MetricContext;
import study.concurrencyproblem.repository.AccountRepository;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class SynchronizedStrategyWithNoTransaction implements LockStrategy {
	private final LockMetrics metrics;
	private final AccountRepository accountRepository;
	private final ConcurrentHashMap<Long, Object> monitors = new ConcurrentHashMap<>();

	public SynchronizedStrategyWithNoTransaction(LockMetrics metrics, AccountRepository accountRepository) {
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
	public Strategy getStrategyType() { return Strategy.SYNCHRONIZED_WITH_NO_TX; }

	private Integer executeWithLock(Long id, ExperimentType experimentType
		, Supplier<Integer> criticalSection) {
		Strategy strategy = getStrategyType();
		Object monitor = monitors.computeIfAbsent(id, k -> new Object());

		MetricContext.set(strategy.name(), experimentType.name());
		try {
			long t0 = System.nanoTime();
			synchronized (monitor) {
				long waited = System.nanoTime() - t0;
				metrics.recordWait(strategy, experimentType, waited);
				return criticalSection.get();
			}
		} finally {
			MetricContext.clear();
		}
	}
}
