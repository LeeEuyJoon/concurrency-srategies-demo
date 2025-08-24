package study.concurrencyproblem.strategy.impl.db.optimistic;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.repository.AccountRepository;

@Component
public class OptimisticTxWorker {

	private final AccountRepository accountRepository;

	public OptimisticTxWorker(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Transactional
	public Integer withdrawOnce(Long id, Integer amount) {
		Account a = accountRepository.findById(id).orElseThrow();
		a.setBalance(a.getBalance() - amount);
		return a.getBalance();
	}

	@Transactional
	public Integer depositOnce(Long id, Integer amount) {
		Account a = accountRepository.findById(id).orElseThrow();
		a.setBalance(a.getBalance() + amount);
		return a.getBalance();
	}

	@Transactional(readOnly = true)
	public Integer getBalance(Long id) {
		return accountRepository.getBalance(id).orElseThrow();
	}
}

