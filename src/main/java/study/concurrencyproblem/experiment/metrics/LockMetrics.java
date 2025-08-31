package study.concurrencyproblem.experiment.metrics;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import study.concurrencyproblem.strategy.Strategy;
import study.concurrencyproblem.experiment.ExperimentType;

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
			.publishPercentileHistogram(true)
			.register(registry)
			.record(nanos, TimeUnit.NANOSECONDS);

	}

	public void recordRetry(Strategy strategy, ExperimentType ep, int retryCount) {
		// 총 재시도 횟수 카운터
		Counter.builder("concurrency.retry.total")
			.description("Total number of retries per operation")
			.tag("strategy", strategy.name())
			.tag("ep", ep.name())
			.register(registry)
			.increment(retryCount);

		// 재시도 분포 측정용 (히스토그램)
		Timer.builder("concurrency.retry.distribution")
			.description("Distribution of retry counts per operation")
			.tag("strategy", strategy.name())
			.tag("ep", ep.name())
			.publishPercentileHistogram(true)
			.register(registry)
			.record(retryCount, TimeUnit.MILLISECONDS);
	}
}
