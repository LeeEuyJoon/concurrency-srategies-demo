package study.concurrencyproblem.strategy.impl.jvm.tx_refactor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.experiment.metrics.MetricContext;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class SynchronizedStrategyRefactor implements LockStrategy {
	private final LockMetrics metrics;
	private final SynchronizedTxWorker worker;
	private final ConcurrentHashMap<Long, Object> monitors = new ConcurrentHashMap<>();

	public SynchronizedStrategyRefactor(LockMetrics metrics, SynchronizedTxWorker worker) {
		this.metrics = metrics;
		this.worker = worker;
	}

	@Override
	public Integer getBalance(Long id, ExperimentType ep) {
		return withMonitor(id, ep, () -> worker.getBalanceTx(id));
	}

	@Override
	public Integer withdraw(Long id, Integer amount, ExperimentType ep) {
		return withMonitor(id, ep, () -> worker.withdrawTx(id, amount));
	}

	@Override
	public Integer deposit(Long id, Integer amount, ExperimentType ep) {
		return withMonitor(id, ep, () -> worker.depositTx(id, amount));
	}

	@Override
	public Strategy getStrategyType() { return Strategy.SYNCHRONIZED_REFACTOR; }

	private <R> R withMonitor(Long id, ExperimentType ep, Supplier<R> body) {
		Strategy strategy = getStrategyType();
		Object monitor = monitors.computeIfAbsent(id, k -> new Object());

		long t0 = System.nanoTime();
		synchronized (monitor) {
			long waited = System.nanoTime() - t0;
			metrics.recordWait(strategy, ep, waited);

			MetricContext.set(strategy.name(), ep.name());
			try {
				return body.get();
			} finally {
				MetricContext.clear();
			}
		}
	}
}
