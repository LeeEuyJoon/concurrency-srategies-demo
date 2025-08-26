package study.concurrencyproblem.strategy.impl.redis;

import static study.concurrencyproblem.strategy.Strategy.*;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.LockMetrics;
import study.concurrencyproblem.experiment.metrics.MetricContext;
import study.concurrencyproblem.repository.RedisLockRepository;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;
import study.concurrencyproblem.strategy.impl.DbTxWorker;

@Component
public class LettuceStrategy implements LockStrategy {

	private static final Duration LEASE_TTL = Duration.ofSeconds(3);
	private static final Integer MAX_WAIT_MS = 3_000;
	private static final Integer BACKOFF_MS = 10;

	private final LockMetrics metrics;
	private final RedisLockRepository lockRepository;
	private final DbTxWorker worker;

	public LettuceStrategy(LockMetrics metrics, RedisLockRepository lockRepository, DbTxWorker worker) {
		this.metrics = metrics;
		this.lockRepository = lockRepository;
		this.worker = worker;
	}

	@Override
	@Transactional(readOnly = true)
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
		return LETTUCE;
	}

	private Integer withRedisLock(Long id, ExperimentType ep, Supplier<Integer> action) {
		MetricContext.set(getStrategyType().name(), ep.name());
		String key		= RedisLockRepository.lockKeyForAccount(id);
		String token	= UUID.randomUUID().toString();

		long t0 = System.nanoTime();
		try {
			long deadline = System.currentTimeMillis() + MAX_WAIT_MS;
			while (true) {
				if (lockRepository.tryLock(key, token, LEASE_TTL))
					break;
				if (System.currentTimeMillis() > deadline) {
					metrics.recordWait(getStrategyType(), ep, System.nanoTime() - t0);
					throw new RuntimeException();
				}
				try {
					Thread.sleep(BACKOFF_MS);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(ie);
				}
			}

			metrics.recordWait(getStrategyType(), ep, System.nanoTime() - t0);

			return action.get();
		} finally {
			try { lockRepository.unlock(key, token); } catch (Exception ignore) {}
			MetricContext.clear();
		}
	}
}
