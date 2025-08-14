package study.concurrencyproblem.experiment.metrics;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Optional;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TxMetricsAspect {
	private final MeterRegistry registry;

	public TxMetricsAspect(MeterRegistry registry) { this.registry = registry; }

	@Around("@annotation(org.springframework.transaction.annotation.Transactional) || " +
		"@within(org.springframework.transaction.annotation.Transactional)")
	public Object measureTx(ProceedingJoinPoint pjp) throws Throwable {
		String strategy = Optional.ofNullable(MetricContext.strategy()).orElse("UNKNOWN");
		String ep       = Optional.ofNullable(MetricContext.ep()).orElse("UNKNOWN");
		boolean readOnly = resolveReadOnly(pjp);

		LongTaskTimer ltt = LongTaskTimer.builder("concurrency.tx.active")
			.description("Active transactions in progress")
			.tag("strategy", strategy).tag("ep", ep)
			.tag("rw", Boolean.toString(readOnly))
			.register(registry);
		LongTaskTimer.Sample active = ltt.start();

		Timer.Sample sample = Timer.start(registry);
		try {
			Object ret = pjp.proceed(); // 내부에서 begin→method→commit/rollback
			sample.stop(Timer.builder("concurrency.tx.duration")
				.description("Transaction open duration")
				.tag("strategy", strategy).tag("ep", ep)
				.tag("rw", Boolean.toString(readOnly))
				.tag("status", "committed")
				.register(registry));
			return ret;
		} catch (Throwable t) {
			sample.stop(Timer.builder("concurrency.tx.duration")
				.description("Transaction open duration")
				.tag("strategy", strategy).tag("ep", ep)
				.tag("rw", Boolean.toString(readOnly))
				.tag("status", "rolled_back")
				.register(registry));
			throw t;
		} finally {
			active.stop();
		}
	}

	private boolean resolveReadOnly(ProceedingJoinPoint pjp) {
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Method method = sig.getMethod();
		try {
			Method targetMethod = pjp.getTarget()
				.getClass()
				.getMethod(method.getName(), method.getParameterTypes());
			Transactional m = AnnotatedElementUtils.findMergedAnnotation(targetMethod, Transactional.class);
			if (m != null) return m.readOnly();
		} catch (NoSuchMethodException ignore) {}
		Transactional c = AnnotatedElementUtils.findMergedAnnotation(pjp.getTarget().getClass(), Transactional.class);
		return c != null && c.readOnly();
	}
}
