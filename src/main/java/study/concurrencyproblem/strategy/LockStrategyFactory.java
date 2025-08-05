package study.concurrencyproblem.strategy;

import org.springframework.stereotype.Component;
import study.concurrencyproblem.strategy.impl.*;


@Component
public class LockStrategyFactory {
    
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