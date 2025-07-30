package study.concurrencyproblem;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import study.concurrencyproblem.domain.Account;
import study.concurrencyproblem.service.AccountService;

@SpringBootTest
@Testcontainers
class AccountServiceTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("concurrency_test")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> false);
    }

    @Autowired
    private AccountService accountService;
    
    private Long testAccountId;

    @BeforeEach
    void setUp() {
        // 테스트용 계좌 생성 (초기 잔액: 10000원)
        Account account = accountService.createAccount(10000);
        testAccountId = account.getId();
        
        System.out.println("초기 잔액: " + accountService.getBalance(testAccountId) + "원");
    }

    @Test
    @DisplayName("단일 스레드 출금 테스트 - 정상 동작")
    void singleThreadWithdrawTest() {
        // Given
        int initialBalance = accountService.getBalance(testAccountId);
        int withdrawAmount = 3000;
        
        // When
        accountService.withdraw(testAccountId, withdrawAmount);
        
        // Then
        int finalBalance = accountService.getBalance(testAccountId);
        assertEquals(initialBalance - withdrawAmount, finalBalance);
        System.out.println("단일 스레드 테스트 - 최종 잔액: " + finalBalance + "원");
    }

    @Test
    @DisplayName("🚨 잔고가 10000원인 계좌에서 두 스레드가 동시에 5000원을 인출")
    void twoThreadWithdraw() throws InterruptedException {
        // Given: 초기 잔액 10000원
        int initialBalance = accountService.getBalance(testAccountId);
        assertEquals(10000, initialBalance);

        int threadCount    = 2;
        int withdrawAmount = 5000;

        ExecutorService executor   = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch  = new CountDownLatch(threadCount);
        CountDownLatch startLatch  = new CountDownLatch(1);
        CountDownLatch doneLatch   = new CountDownLatch(threadCount);

        // When: 두 스레드가 동시에 인출을 시도할 때
        for (int i = 0; i < threadCount; i++) {
            final int threadNumber = i + 1;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();

                    // 각 스레드가 읽은 잔고
                    int balanceRead = accountService.getBalance(testAccountId);
                    System.out.printf("Thread-%d 읽은 잔고: %d원%n", threadNumber, balanceRead);

                    // 인출 시도
                    accountService.withdraw(testAccountId, withdrawAmount);

                    // 각 스레드가 인출한 후 잔고
                    int balanceAfter = accountService.getBalance(testAccountId);
                    System.out.printf("Thread-%d 인출 후 잔고: %d원%n", threadNumber, balanceAfter);

                } catch (InterruptedException ignored) {
                } finally {
                    // 4) 완료 알림
                    doneLatch.countDown();
                }
            });
        }

        // 모든 워커가 준비될 때까지 대기
        readyLatch.await();
        // 동시에 출금 시작
        startLatch.countDown();
        // 모든 워커가 끝날 때까지 대기
        doneLatch.await();
        // ExecutorService 종료
        executor.shutdown();

        // Then: 최종 잔액이 0원이 아닌 5000원
        int finalBalance = accountService.getBalance(testAccountId);
        System.out.printf("초기 잔액=%d, 최종 잔액=%d%n", initialBalance, finalBalance);

        assertEquals(
            initialBalance - withdrawAmount,
            finalBalance
        );
    }

    @Test
    @DisplayName("🚨 잔고가 10000원인 계좌에서 100개의 스레드가 동시에 100원씩 인출")
    void oneHundredThreadWithdraw() throws InterruptedException {
        // Given: 초기 잔액 10000원
        int initialBalance = accountService.getBalance(testAccountId);
        assertEquals(10000, initialBalance);

        int threadCount    = 100;
        int withdrawAmount = 100;

        ExecutorService executor   = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch  = new CountDownLatch(threadCount);
        CountDownLatch startLatch  = new CountDownLatch(1);
        CountDownLatch doneLatch   = new CountDownLatch(threadCount);

        // When: 100개의 스레드가 동시에 인출을 시도할 때
        for (int i = 0; i < threadCount; i++) {
            final int threadNumber = i + 1;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();

                    // 각 스레드가 읽은 잔고
                    int balanceRead = accountService.getBalance(testAccountId);
                    System.out.printf("Thread-%d 읽은 잔고: %d원%n", threadNumber, balanceRead);

                    // 인출 시도
                    accountService.withdraw(testAccountId, withdrawAmount);

                    // 각 스레드가 인출한 후 잔고
                    int balanceAfter = accountService.getBalance(testAccountId);
                    System.out.printf("Thread-%d 인출 후 잔고: %d원%n", threadNumber, balanceAfter);

                } catch (InterruptedException ignored) {
                } finally {
                    // 4) 완료 알림
                    doneLatch.countDown();
                }
            });
        }

        // 모든 워커가 준비될 때까지 대기
        readyLatch.await();
        // 동시에 출금 시작
        startLatch.countDown();
        // 모든 워커가 끝날 때까지 대기
        doneLatch.await();
        // ExecutorService 종료
        executor.shutdown();

        // Then: 최종 잔액이 0원이 아님
        int finalBalance = accountService.getBalance(testAccountId);
        System.out.printf("초기 잔액=%d, 최종 잔액=%d%n", initialBalance, finalBalance);

        assertTrue(finalBalance > 0);
    }


} 