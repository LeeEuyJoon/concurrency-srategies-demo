package study.concurrencyproblem.strategy;

public enum Strategy {
    NO_USE,
    NO_LOCK,
    SYNCHRONIZED,
    SYNCHRONIZED_WITH_NO_TX,
    SYNCHRONIZED_REFACTOR,
    REENTRANT_LOCK,
    REENTRANT_LOCK_WITH_NO_TX,
    REENTRANT_READ_WRITE_LOCK,
    STAMPED_LOCK
}
