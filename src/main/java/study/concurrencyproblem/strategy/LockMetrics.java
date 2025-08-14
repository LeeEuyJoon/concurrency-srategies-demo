package study.concurrencyproblem.strategy;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import study.experiment.ExperimentType;

@Component
public class LockMetrics {
	private final MeterRegistry registry;

	public LockMetrics(MeterRegistry registry) {
		this.registry = registry;
	}

	public void recordWait(Strategy strategy, ExperimentType ep, long nanos) {
		Timer.builder("concurrency.lock.wait")
			.description("Time spent waiting to acquire lock")
			.tag("strategy", strategy.name())
			.tag("ep", ep.name())
			.register(registry)
			.record(nanos, TimeUnit.NANOSECONDS);

	}
}
