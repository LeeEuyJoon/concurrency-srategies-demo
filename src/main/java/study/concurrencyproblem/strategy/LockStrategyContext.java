package study.concurrencyproblem.strategy;

import org.springframework.stereotype.Component;

@Component
public class LockStrategyContext {
	private LockStrategy strategy;

	public void setStrategy(LockStrategy strategy) {
		this.strategy = strategy;
	}

	public Integer getBalance(Long id) {
		return strategy.getBalance(id);
	}
	public Integer withdraw(Long id, Integer amount) {
		return strategy.withdraw(id, amount);
	}
	public Integer deposit(Long id, Integer amount) {
		return strategy.deposit(id, amount);
	}
}

