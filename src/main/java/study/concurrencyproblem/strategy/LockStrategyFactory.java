package study.concurrencyproblem.strategy;

import org.springframework.stereotype.Component;
import study.concurrencyproblem.strategy.impl.*;

/**
 * LockStrategy 객체를 생성하는 팩토리 클래스
 * Strategy enum에 따라 적절한 LockStrategy 구현체를 반환
 */
@Component
public class LockStrategyFactory {
    
    // 전략 구현체들
    private final NoLockStrategy noLockStrategy;
    private final SynchronizedStrategy synchronizedStrategy;
    private final ReentrantLockStrategy reentrantLockStrategy;
    private final ReentrantReadWriteLockStrategy reentrantReadWriteLockStrategy;
    private final StampedLockStrategy stampedLockStrategy;
    
    public LockStrategyFactory(NoLockStrategy noLockStrategy,
                              SynchronizedStrategy synchronizedStrategy,
                              ReentrantLockStrategy reentrantLockStrategy,
                              ReentrantReadWriteLockStrategy reentrantReadWriteLockStrategy,
                              StampedLockStrategy stampedLockStrategy) {
        this.noLockStrategy = noLockStrategy;
        this.synchronizedStrategy = synchronizedStrategy;
        this.reentrantLockStrategy = reentrantLockStrategy;
        this.reentrantReadWriteLockStrategy = reentrantReadWriteLockStrategy;
        this.stampedLockStrategy = stampedLockStrategy;
    }
    
    /**
     * Strategy enum에 따라 적절한 LockStrategy 구현체를 반환
     * @param strategy 락 전략 타입
     * @return 해당하는 LockStrategy 구현체
     * @throws IllegalArgumentException 지원하지 않는 전략인 경우
     */
    public LockStrategy create(Strategy strategy) {
        switch (strategy) {
            case NO_LOCK:
                return noLockStrategy;
            case SYNCHRONIZED:
                return synchronizedStrategy;
            case REENTRANT_LOCK:
                return reentrantLockStrategy;
            case REENTRANT_READ_WRITE_LOCK:
                return reentrantReadWriteLockStrategy;
            case STAMPED_LOCK:
                return stampedLockStrategy;
            default:
                throw new IllegalArgumentException("지원하지 않는 전략: " + strategy);
        }
    }
}