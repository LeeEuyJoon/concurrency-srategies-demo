package study.concurrencyproblem.strategy.impl.jvm.refactor;

import java.util.function.Supplier;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.strategy.LockStrategy;

public abstract class AbstractLockStrategy implements LockStrategy {
	protected final LockMetrics metrics;
	protected final TxWorker worker;

	protected AbstractLockStrategy(LockMetrics metrics, TxWorker worker) {
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

	protected abstract <R> R executeWithLock(Long id, ExperimentType ep, Supplier<R> body);
}
