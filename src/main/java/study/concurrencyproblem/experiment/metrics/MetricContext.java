package study.concurrencyproblem.experiment.metrics;

public final class MetricContext {
	private static final ThreadLocal<String> STRATEGY = new ThreadLocal<>();
	private static final ThreadLocal<String> EP = new ThreadLocal<>();

	private MetricContext(){}

	public static void set(String strategy, String ep) {
		STRATEGY.set(strategy);
		EP.set(ep);
	}
	public static String strategy() { return STRATEGY.get(); }
	public static String ep() { return EP.get(); }
	public static void clear() { STRATEGY.remove(); EP.remove(); }
}