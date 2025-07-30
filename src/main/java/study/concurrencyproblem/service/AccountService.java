package study.concurrencyproblem.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.repository.AccountRepository;

@Service
@Transactional
public class AccountService implements AccountServiceInterface {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // 계좌 생성
    @Override
    public Account createAccount(Integer initialBalance) {
        Account account = new Account(initialBalance);
        return accountRepository.save(account);
    }

    // 잔액 조회
    @Override
    @Transactional(readOnly = true)
    public Integer getBalance(Long id) {
        return accountRepository.getBalance(id)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));
    }

    // 출금
    @Override
    public Integer withdraw(Long id, Integer amount) {
        Account account = accountRepository.findById(id).orElseThrow();
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
        return account.getBalance();
    }

    // 예금
    @Override
    public Integer deposit(Long id, Integer amount) {
        Account account = accountRepository.findById(id).orElseThrow();
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        return account.getBalance();
    }
} 