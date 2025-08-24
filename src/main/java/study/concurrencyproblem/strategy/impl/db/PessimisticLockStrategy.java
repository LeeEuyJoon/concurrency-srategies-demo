package study.concurrencyproblem.strategy.impl.db;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.util.function.Function;

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
public class PessimisticLockStrategy implements LockStrategy {

	private final LockMetrics metrics;
	private final AccountRepository accountRepository;

	public PessimisticLockStrategy(LockMetrics metrics, AccountRepository accountRepository) {
		this.metrics = metrics;
		this.accountRepository = accountRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Integer getBalance(Long id, ExperimentType ep) {
		return accountRepository.getBalance(id).orElseThrow();
	}

	@Override
	@Transactional
	public Integer withdraw(Long id, Integer amount, ExperimentType ep) {
		return withLockedAccount(id, ep, a -> {
			a.setBalance(a.getBalance() - amount);
			return a.getBalance();
		});
	}

	@Override
	@Transactional
	public Integer deposit(Long id, Integer amount, ExperimentType ep) {
		return withLockedAccount(id, ep, a -> {
			a.setBalance(a.getBalance() + amount);
			return a.getBalance();
		});
	}

	@Override
	public Strategy getStrategyType() { return DB_PESSIMISTIC; }

	private Integer withLockedAccount(Long id, ExperimentType ep, Function<Account, Integer> body) {
		MetricContext.set(DB_PESSIMISTIC.name(), ep.name());
		try {
			long t0 = System.nanoTime();
			Account a = accountRepository.findByIdWithPessimisticLock(id).orElseThrow();
			metrics.recordWait(DB_PESSIMISTIC, ep, System.nanoTime() - t0);
			return body.apply(a);
		} finally {
			MetricContext.clear();
		}
	}
}