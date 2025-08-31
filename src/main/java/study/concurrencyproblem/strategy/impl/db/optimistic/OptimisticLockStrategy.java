package study.concurrencyproblem.strategy.impl.db.optimistic;

import static study.concurrencyproblem.strategy.Strategy.DB_OPTIMISTIC;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.experiment.metrics.MetricContext;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;
import study.concurrencyproblem.strategy.impl.DbTxWorker;

@Component
public class OptimisticLockStrategy implements LockStrategy {

	private static final int MAX_RETRY = 30;
	private static final long RETRY_TERM = 10;

	private final LockMetrics metrics;
	private final DbTxWorker worker;

	public OptimisticLockStrategy(LockMetrics metrics, DbTxWorker worker) {
		this.metrics = metrics;
		this.worker = worker;
	}

	@Override
	public Integer getBalance(Long id, ExperimentType ep) {
		MetricContext.set(DB_OPTIMISTIC.name(), ep.name());
		try {
			metrics.recordWait(DB_OPTIMISTIC, ep, 0L);
			return worker.getBalance(id);
		} finally {
			MetricContext.clear();
		}
	}

	@Override
	public Integer withdraw(Long id, Integer amount, ExperimentType ep) {
		return runWithRetry(ep, () -> worker.withdrawOnce(id, amount));
	}

	@Override
	public Integer deposit(Long id, Integer amount, ExperimentType ep) {
		return runWithRetry(ep, () -> worker.depositOnce(id, amount));
	}

	@Override public Strategy getStrategyType() { return DB_OPTIMISTIC; }

	private Integer runWithRetry(ExperimentType ep, Supplier<Integer> attempt) {
		MetricContext.set(getStrategyType().name(), ep.name());
		try {
			metrics.recordWait(getStrategyType(), ep, 0L);
			int tries = 0;
			while (true) {
				try {
					Integer result = attempt.get();
					metrics.recordRetry(getStrategyType(), ep, tries);
					return result;
				} catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
					tries++;
					if (tries > MAX_RETRY) {
						metrics.recordRetry(getStrategyType(), ep, tries);
						throw e;
					}
					try {
						Thread.sleep(RETRY_TERM);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						throw new RuntimeException(ie);
					}
				}
			}
		} finally {
			MetricContext.clear();
		}
	}
}
