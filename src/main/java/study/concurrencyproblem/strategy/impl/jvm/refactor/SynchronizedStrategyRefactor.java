package study.concurrencyproblem.strategy.impl.jvm.refactor;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.experiment.metrics.MetricContext;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;

@Component
public class SynchronizedStrategyRefactor extends AbstractLockStrategy {
	private final ConcurrentHashMap<Long, Object> monitors = new ConcurrentHashMap<>();

	public SynchronizedStrategyRefactor(LockMetrics metrics, TxWorker worker) {
		super(metrics, worker);
	}

	@Override
	public Strategy getStrategyType() { return SYNCHRONIZED_REFACTOR; }

	@Override
	protected <R> R executeWithLock(Long id, ExperimentType ep, Supplier<R> body) {
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
