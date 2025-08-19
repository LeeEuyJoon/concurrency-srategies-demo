package study.concurrencyproblem.strategy.impl.jvm.refactor;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class ReentrantReadWriteLockStrategyRefactor implements LockStrategy {
	private final ConcurrentHashMap<Long, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();
	protected final LockMetrics metrics;
	protected final TxWorker worker;

	public ReentrantReadWriteLockStrategyRefactor(LockMetrics metrics, TxWorker worker) {
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

	private <R> R executeWithLock(Long id, ExperimentType ep, Supplier<R> body, boolean isWrite) {
		Strategy strategy = getStrategyType();
		ReentrantReadWriteLock lock = locks.computeIfAbsent(id, k -> new ReentrantReadWriteLock());

		long t0 = System.nanoTime();

		if (isWrite) {
			lock.writeLock().lock();
		} else {
			lock.readLock().lock();
		}

		try {
			long waited = System.nanoTime() - t0;
			metrics.recordWait(strategy, ep, waited);
			return body.get();
		} finally {
			if (isWrite) {
				lock.writeLock().unlock();
			} else {
				lock.readLock().unlock();
			}
		}
	}

	@Override
	public Strategy getStrategyType() {
		return REENTRANT_READ_WRITE_LOCK_REFACTOR;
	}
}
