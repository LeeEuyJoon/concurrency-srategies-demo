package study.concurrencyproblem.applock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("study.concurrencyproblem.core.domain") // core 모듈의 엔티티 스캔
public class AppLockApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppLockApplication.class, args);
    }
} 