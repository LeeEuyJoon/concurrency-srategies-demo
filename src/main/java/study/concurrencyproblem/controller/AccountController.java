package study.concurrencyproblem.controller;

import org.springframework.web.bind.annotation.*;


import study.concurrencyproblem.dto.TransactionRequest;
import study.concurrencyproblem.service.AccountServiceInterface;
import study.concurrencyproblem.strategy.LockStrategy;
import study.concurrencyproblem.strategy.LockStrategyContext;
import study.concurrencyproblem.strategy.LockStrategyFactory;
import study.concurrencyproblem.strategy.Strategy;

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
                             @RequestParam(defaultValue = "NO_LOCK") Strategy strategy) {
        LockStrategy selectedStrategy = lockStrategyFactory.create(strategy);
        lockStrategyContext.setStrategy(selectedStrategy);

        return lockStrategyContext.getBalance(id);
    }
    
    // 출금
    @PostMapping("/{id}/withdraw")
    public Integer withdraw(@PathVariable Long id, @RequestBody TransactionRequest request) {

        LockStrategy selectedStrategy = lockStrategyFactory.create(request.getStrategy());
        lockStrategyContext.setStrategy(selectedStrategy);

        return lockStrategyContext.withdraw(id, request.getAmount());
    }
    
    // 입금  
    @PostMapping("/{id}/deposit")
    public Integer deposit(@PathVariable Long id, @RequestBody TransactionRequest request) {

        LockStrategy selectedStrategy = lockStrategyFactory.create(request.getStrategy());
        lockStrategyContext.setStrategy(selectedStrategy);

        return lockStrategyContext.deposit(id, request.getAmount());
    }
}