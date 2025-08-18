package study.concurrencyproblem.strategy.impl.jvm.tx_refactor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.repository.AccountRepository;

@Component
public class SynchronizedTxWorker {
	private final AccountRepository accountRepository;

	public SynchronizedTxWorker(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Transactional
	public Integer withdrawTx(Long id, Integer amount) {
		Account account = accountRepository.findById(id).orElseThrow();
		account.setBalance(account.getBalance() - amount);
		accountRepository.save(account);
		return account.getBalance();
	}

	@Transactional
	public Integer depositTx(Long id, Integer amount) {
		Account account = accountRepository.findById(id).orElseThrow();
		account.setBalance(account.getBalance() + amount);
		accountRepository.save(account);
		return account.getBalance();
	}

	@Transactional(readOnly = true)
	public Integer getBalanceTx(Long id) {
		return accountRepository.getBalance(id).orElseThrow();
	}
}
