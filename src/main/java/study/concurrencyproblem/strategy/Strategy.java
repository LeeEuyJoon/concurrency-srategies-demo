package study.concurrencyproblem.strategy;

public enum Strategy {
    NO_LOCK,
    SYNCHRONIZED,
    REENTRANT_LOCK,
    REENTRANT_READ_WRITE_LOCK,
    STAMPED_LOCK
}
