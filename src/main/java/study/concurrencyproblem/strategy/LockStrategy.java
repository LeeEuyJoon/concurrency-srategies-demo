package study.concurrencyproblem.strategy;

public interface LockStrategy {
    
    // 잔액 조회
    Integer getBalance(Long id);

    // 출금
    Integer withdraw(Long id, Integer amount);

    // 입금
    Integer deposit(Long id, Integer amount);

    // 락 타입 반환
    Strategy getStrategyType();
}
