package study.concurrencyproblem.strategy.impl.jvm;

import static study.concurrencyproblem.strategy.Strategy.*;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.repository.AccountRepository;
import study.concurrencyproblem.experiment.metrics.MetricContext;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;
import study.concurrencyproblem.experiment.ExperimentType;

@Component
public class NoLockStrategy implements LockStrategy {
	private final AccountRepository accountRepository;

	public NoLockStrategy(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	// 계좌 생성
	public Account createAccount(Integer initialBalance) {
		Account account = new Account(initialBalance);
		return accountRepository.save(account);
	}

	// 잔액 조회
	@Override
	@Transactional
    public Integer getBalance(Long id, ExperimentType experimentType) {
		MetricContext.set(getStrategyType().name(), experimentType.name());
		try {
			return accountRepository.getBalance(id).
				orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없음"));
		} finally {
			MetricContext.clear();
		}
    }

	// 출금
	@Override
	@Transactional
	public Integer withdraw(Long id, Integer amount, ExperimentType experimentType) {
		MetricContext.set(getStrategyType().name(), experimentType.name());
		try {
			Account account = accountRepository.findById(id).orElseThrow();
			account.setBalance(account.getBalance() - amount);
			accountRepository.save(account);
			return account.getBalance();
		} finally {
			MetricContext.clear();
		}
	}

	// 예금
	@Override
	@Transactional
	public Integer deposit(Long id, Integer amount, ExperimentType experimentType) {
		MetricContext.set(getStrategyType().name(), experimentType.name());
		try {
			Account account = accountRepository.findById(id).orElseThrow();
			account.setBalance(account.getBalance() + amount);
			accountRepository.save(account);
			return account.getBalance();
		} finally {
			MetricContext.clear();
		}
	}

	@Override
	public Strategy getStrategyType() {
		return NO_LOCK;
	}
}
