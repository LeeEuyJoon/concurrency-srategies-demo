package study.concurrencyproblem.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import study.concurrencyproblem.service.AccountServiceInterface;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final AccountServiceInterface accountService;
    
    public DataInitializer(AccountServiceInterface accountService) {
        this.accountService = accountService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 테스트용 계좌 생성
        for (int i = 0; i < 3; i++) {
            accountService.createAccount(100000);
        }
    }
}