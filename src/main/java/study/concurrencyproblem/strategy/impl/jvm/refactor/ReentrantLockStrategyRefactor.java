package study.concurrencyproblem.strategy.impl.jvm.refactor;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class ReentrantLockStrategyRefactor implements LockStrategy {
	private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();
	protected final LockMetrics metrics;
	protected final TxWorker worker;

	public ReentrantLockStrategyRefactor(LockMetrics metrics, TxWorker worker) {
		this.metrics = metrics;
		this.worker = worker;
	}

	@Override
	public Integer getBalance(Long id, ExperimentType ep) {
		return executeWithLock(id, ep, () -> worker.getBalanceTx(id));
	}

	@Override
	public Integer withdraw(Long id, Integer amount, ExperimentType ep) {
		return executeWithLock(id, ep, () -> worker.withdrawTx(id, amount));
	}

	@Override
	public Integer deposit(Long id, Integer amount, ExperimentType ep) {
		return executeWithLock(id, ep, () -> worker.depositTx(id, amount));
	}

	@Override
	public Strategy getStrategyType() {
		return REENTRANT_LOCK_REFACTOR;
	}

	private <R> R executeWithLock(Long id, ExperimentType ep, Supplier<R> body) {
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
