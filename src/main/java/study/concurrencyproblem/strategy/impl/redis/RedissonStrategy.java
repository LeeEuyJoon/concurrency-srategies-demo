package study.concurrencyproblem.strategy.impl.redis;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.experiment.metrics.MetricContext;
import study.concurrencyproblem.repository.RedisLockRepository;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;
import study.concurrencyproblem.strategy.impl.DbTxWorker;

@Component
public class RedissonStrategy implements LockStrategy {

	private static final Duration LEASE_TTL = Duration.ofSeconds(3);
	private static final Duration MAX_WAIT  = Duration.ofSeconds(3);

	private final RedissonClient redisson;
	private final LockMetrics metrics;
	private final DbTxWorker worker;

	public RedissonStrategy(RedissonClient redisson, LockMetrics metrics, DbTxWorker worker) {
		this.redisson = redisson;
		this.metrics  = metrics;
		this.worker   = worker;
	}


	@Override
	public Integer getBalance(Long id, ExperimentType ep) {
		return worker.getBalance(id);
	}

	@Override
	public Integer withdraw(Long id, Integer amount, ExperimentType ep) {
		return withRedisLock(id, ep, () -> worker.withdrawOnce(id, amount));
	}

	@Override
	public Integer deposit(Long id, Integer amount, ExperimentType ep) {
		return withRedisLock(id, ep, () -> worker.depositOnce(id, amount));
	}

	@Override
	public Strategy getStrategyType() {
		return REDISSON;
	}

	private Integer withRedisLock(Long id, ExperimentType ep, Supplier<Integer> action) {
		MetricContext.set(getStrategyType().name(), ep.name());
		String key 	= RedisLockRepository.lockKeyForAccount(id);
		RLock lock 	= redisson.getLock(key);

		boolean acquired = false;
		long t0 = System.nanoTime();
		try {
			acquired = lock.tryLock(
				MAX_WAIT.toMillis(),
				LEASE_TTL.toMillis(),
				TimeUnit.MILLISECONDS
			);
			long waited = System.nanoTime() - t0;
			metrics.recordWait(getStrategyType(), ep, waited);

			if (!acquired) {
				throw new RuntimeException();
			}

			return action.get();

	} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			throw new RuntimeException();
		} finally {
			try {
				if (acquired && lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			} finally {
				MetricContext.clear();
			}
		}
	}

}
