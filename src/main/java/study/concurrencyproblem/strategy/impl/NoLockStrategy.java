package study.concurrencyproblem.strategy.impl;

import org.springframework.stereotype.Component;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.repository.AccountRepository;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.Strategy;
import study.experiment.ExperimentType;

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
    public Integer getBalance(Long id, ExperimentType experimentType) {
		return accountRepository.getBalance(id).
			orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없음"));
    }

	// 출금
	@Override
	public Integer withdraw(Long id, Integer amount, ExperimentType experimentType) {
		Account account = accountRepository.findById(id).orElseThrow();
		account.setBalance(account.getBalance() - amount);
		accountRepository.save(account);
		return account.getBalance();
	}

	// 예금
	@Override
	public Integer deposit(Long id, Integer amount, ExperimentType experimentType) {
		Account account = accountRepository.findById(id).orElseThrow();
		account.setBalance(account.getBalance() + amount);
		accountRepository.save(account);
		return account.getBalance();
	}

	@Override
	public Strategy getStrategyType() {
		return Strategy.NO_LOCK;
	}
}
