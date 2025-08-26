package study.concurrencyproblem.strategy.impl.db.named;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.experiment.metrics.MetricContext;
import study.concurrencyproblem.repository.AccountRepository;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;
import study.concurrencyproblem.strategy.impl.DbTxWorker;

@Component
public class NamedLockStrategyRefactor implements LockStrategy {
	private static final Integer LOCK_TIMEOUT_SEC = 3;
	private static final String KEY_PREFIX = "acc:";

	private final LockMetrics metrics;
	private final AccountRepository accountRepository;
	private final DbTxWorker worker;

	public NamedLockStrategyRefactor(LockMetrics metrics, AccountRepository accountRepository, DbTxWorker worker) {
		this.metrics = metrics;
		this.accountRepository = accountRepository;
		this.worker = worker;
	}

	@Override
	@Transactional(readOnly = true)
	public Integer getBalance(Long id, ExperimentType ep) {
		return worker.getBalance(id);
	}

	@Override
	public Integer withdraw(Long id, Integer amount, ExperimentType ep) {
		return withNamedLock(id, ep, () -> worker.withdrawOnce(id, amount));
	}

	@Override
	public Integer deposit(Long id, Integer amount, ExperimentType ep) {
		return withNamedLock(id, ep, () -> worker.depositOnce(id, amount));
	}

	@Override
	public Strategy getStrategyType() {
		return DB_NAMED;
	}

	private Integer withNamedLock(Long id, ExperimentType ep, Supplier<Integer> body) {
		MetricContext.set(getStrategyType().name(), ep.name());
		String key = KEY_PREFIX + id;

		try {
			long t0 = System.nanoTime();
			Integer ok = accountRepository.getLock(key, LOCK_TIMEOUT_SEC);
			long waited = System.nanoTime() - t0;
			metrics.recordWait(getStrategyType(), ep, waited);

			if (ok == null) {
				throw new IllegalStateException("GET_LOCK returned NULL (error) for key=" + key);
			}
			if (ok == 0) {
				throw new RuntimeException("Named lock timeout: " + key);
			}

			return body.get();
		} finally {
			try {
				accountRepository.releaseLock(key);
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
			MetricContext.clear();
		}
	}
}
