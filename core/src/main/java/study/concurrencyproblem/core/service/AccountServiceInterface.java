package study.concurrencyproblem.core.service;

import study.concurrencyproblem.core.domain.Account;

/**
 * 동시성 실험을 위한 공통 계좌 서비스 인터페이스
 */
public interface AccountServiceInterface {
    
    /**
     * 계좌 생성
     */
    Account createAccount(Integer initialBalance);
    
    /**
     * 잔액 조회
     */
    Integer getBalance(Long id);
    
    /**
     * 출금
     */
    Integer withdraw(Long id, Integer amount);
    
    /**
     * 예금  
     */
    Integer deposit(Long id, Integer amount);
} 