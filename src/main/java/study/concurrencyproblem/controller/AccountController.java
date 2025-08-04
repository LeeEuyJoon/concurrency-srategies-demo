package study.concurrencyproblem.controller;

import org.springframework.web.bind.annotation.*;
import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.dto.CreateAccountRequest;
import study.concurrencyproblem.dto.TransactionRequest;
import study.concurrencyproblem.service.AccountServiceInterface;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    
    private final AccountServiceInterface accountService;
    
    public AccountController(AccountServiceInterface accountService) {
        this.accountService = accountService;
    }
    
    // 계좌 생성
    @PostMapping
    public Account createAccount(@RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request.getInitialBalance());
    }
    
    // 잔액 조회
    @GetMapping("/{id}/balance")
    public Integer getBalance(@PathVariable Long id) {
        return accountService.getBalance(id);
    }
    
    // 출금
    @PostMapping("/{id}/withdraw")
    public Integer withdraw(@PathVariable Long id, @RequestBody TransactionRequest request) {
        return accountService.withdraw(id, request.getAmount());
    }
    
    // 입금  
    @PostMapping("/{id}/deposit")
    public Integer deposit(@PathVariable Long id, @RequestBody TransactionRequest request) {
        return accountService.deposit(id, request.getAmount());
    }
    
}