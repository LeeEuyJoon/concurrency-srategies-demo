package study.concurrencyproblem.strategy.impl.jvm.tx;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.repository.AccountRepository;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;
import study.concurrencyproblem.experiment.ExperimentType;

@Component
public class SynchronizedStrategy implements LockStrategy {

	private final LockMetrics metrics;
	private final AccountRepository accountRepository;
	private final ConcurrentHashMap<Long, Object> monitors = new ConcurrentHashMap<>();

	public SynchronizedStrategy(LockMetrics metrics, AccountRepository accountRepository) {
		this.metrics = metrics;
		this.accountRepository = accountRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Integer getBalance(Long id, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () ->
			accountRepository.getBalance(id).orElseThrow()
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
		});
	}

	@Override
	@Transactional
	public Integer deposit(Long id, Integer amount, ExperimentType experimentType) {
		return executeWithLock(id, experimentType, () -> {
			Account account = accountRepository.findById(id).orElseThrow();
			account.setBalance(account.getBalance() + amount);
			accountRepository.save(account);
			return account.getBalance();
		});
	}

	@Override
	public Strategy getStrategyType() { return Strategy.SYNCHRONIZED; }

	private Integer executeWithLock(Long id, ExperimentType experimentType
									, Supplier<Integer> criticalSection) {
		Strategy strategy = getStrategyType();
		Object monitor = monitors.computeIfAbsent(id, k -> new Object());

		long t0 = System.nanoTime();
		synchronized (monitor) {
			long waited = System.nanoTime() - t0;
			metrics.recordWait(strategy, experimentType, waited);
			return criticalSection.get();
		}
	}
}
