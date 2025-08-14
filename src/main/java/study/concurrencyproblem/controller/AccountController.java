package study.concurrencyproblem.controller;

import org.springframework.web.bind.annotation.*;


import study.concurrencyproblem.dto.TransactionRequest;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.LockStrategyContext;
import study.concurrencyproblem.strategy.LockStrategyFactory;
import study.concurrencyproblem.strategy.Strategy;
import study.concurrencyproblem.experiment.ExperimentType;
import study.concurrencyproblem.experiment.metrics.MetricContext;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    
    private final LockStrategyContext lockStrategyContext;
    private final LockStrategyFactory lockStrategyFactory;

    public AccountController(
                           LockStrategyContext lockStrategyContext,
                           LockStrategyFactory lockStrategyFactory) {
        this.lockStrategyContext = lockStrategyContext;
        this.lockStrategyFactory = lockStrategyFactory;
    }

    // 잔액 조회
    @GetMapping("/{id}/balance")
    public Integer getBalance(@PathVariable Long id,
        @RequestParam(defaultValue = "NO_LOCK") Strategy strategy,
        @RequestParam(defaultValue = "WITHDRAW_ONLY") ExperimentType experimentType) {
        LockStrategy selectedStrategy = lockStrategyFactory.create(strategy);
        lockStrategyContext.setStrategy(selectedStrategy);

        MetricContext.set(strategy.name(), experimentType.name());
        try {
            return lockStrategyContext.getBalance(id, experimentType);
        } finally {
            MetricContext.clear(); // 누수 방지
        }
    }
    
    // 출금
    @PostMapping("/{id}/withdraw")
    public Integer withdraw(@PathVariable Long id, @RequestBody TransactionRequest request) {
        Strategy strategy = request.getStrategy();
        ExperimentType experimentType = request.getExperimentType();

        LockStrategy selectedStrategy = lockStrategyFactory.create(strategy);
        lockStrategyContext.setStrategy(selectedStrategy);

        MetricContext.set(strategy.name(), experimentType.name());
        try {
            return lockStrategyContext.withdraw(id, request.getAmount(), experimentType);
        } finally {
            MetricContext.clear();
        }
    }
    
    // 입금  
    @PostMapping("/{id}/deposit")
    public Integer deposit(@PathVariable Long id, @RequestBody TransactionRequest request) {
        Strategy strategy = request.getStrategy();
        ExperimentType experimentType = request.getExperimentType();

        LockStrategy selectedStrategy = lockStrategyFactory.create(strategy);
        lockStrategyContext.setStrategy(selectedStrategy);

        MetricContext.set(strategy.name(), experimentType.name());
        try {
            return lockStrategyContext.deposit(id, request.getAmount(), experimentType);
        } finally {
            MetricContext.clear();
        }
    }
}