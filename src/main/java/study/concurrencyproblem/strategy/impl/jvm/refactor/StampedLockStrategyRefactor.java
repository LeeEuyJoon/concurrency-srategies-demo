package study.concurrencyproblem.strategy.impl.jvm.refactor;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class StampedLockStrategyRefactor implements LockStrategy {
	protected final LockMetrics metrics;
	protected final TxWorker worker;
	private final ConcurrentHashMap<Long, StampedLock> locks = new ConcurrentHashMap<>();

	public StampedLockStrategyRefactor(LockMetrics metrics, TxWorker worker) {
		this.metrics = metrics;
		this.worker = worker;
	}

	@Override
	public Integer getBalance(Long id, ExperimentType ep) {
		return executeWithLock(id, ep, () -> worker.getBalanceTx(id), false);
	}

	@Override
	public Integer withdraw(Long id, Integer amount, ExperimentType ep) {
		return executeWithLock(id, ep, () -> worker.withdrawTx(id, amount), true);
	}

	@Override
	public Integer deposit(Long id, Integer amount, ExperimentType ep) {
		return executeWithLock(id, ep, () -> worker.depositTx(id, amount), true);
	}

	@Override
	public Strategy getStrategyType() {
		return STAMPED_LOCK_REFACTOR;
	}

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
