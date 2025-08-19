package study.concurrencyproblem.strategy.impl.jvm.refactor;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class ReentrantLockRefactor extends AbstractLockStrategy {
	private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

	public ReentrantLockRefactor(LockMetrics metrics, TxWorker worker) {
		super(metrics, worker);
	}

	@Override
	public Strategy getStrategyType() {
		return REENTRANT_LOCK_REFACTOR;
	}

	protected <R> R executeWithLock(Long id, ExperimentType ep, Supplier<R> body) {
		Strategy strategy = getStrategyType();
		ReentrantLock lock = locks.computeIfAbsent(id, k -> new ReentrantLock());

		long t0 = System.nanoTime();
		lock.lock();
		try {
			long waited = System.nanoTime() - t0;
			metrics.recordWait(strategy, ep, waited);
			return body.get();
		} finally {
			lock.unlock();
		}
	}
}
