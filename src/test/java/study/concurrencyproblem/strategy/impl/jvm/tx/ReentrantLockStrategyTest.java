package study.concurrencyproblem.strategy.impl.jvm.tx;

import static org.junit.jupiter.api.Assertions.*;
import static study.concurrencyproblem.experiment.ExperimentType.*;

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
import study.concurrencyproblem.strategy.impl.jvm.NoLockStrategy;

@SpringBootTest
@Testcontainers
class ReentrantLockStrategyTest {
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
	private ReentrantLockStrategy strategy;
	@Autowired
	private NoLockStrategy noLockStrategy;

	private Long testAccountId;

	@BeforeEach
	void setUp() {
		// 테스트용 계좌 생성 (초기 잔액: 100,000원)
		Account account = noLockStrategy.createAccount(100_000);
		testAccountId = account.getId();

		System.out.println("초기 잔액: " + strategy.getBalance(testAccountId, WITHDRAW_ONLY) + "원");
	}

	@Test
	@DisplayName("단일 스레드 출금 테스트 - 정상 동작")
	void singleThreadWithdrawTest() {
		// Given
		int initialBalance = strategy.getBalance(testAccountId, WITHDRAW_ONLY);
		int withdrawAmount = 3000;

		// When
		strategy.withdraw(testAccountId, withdrawAmount, WITHDRAW_ONLY);

		// Then
		int finalBalance = strategy.getBalance(testAccountId, WITHDRAW_ONLY);
		assertEquals(initialBalance - withdrawAmount, finalBalance);
		System.out.println("단일 스레드 테스트 - 최종 잔액: " + finalBalance + "원");
	}

	@Test
	@DisplayName("Reentrant Vanilla - 잔고가 100,000원인 계좌에서 두 스레드가 동시에 50,000원을 인출")
	void twoThreadWithdraw() throws InterruptedException {
		// [ Given: 초기 잔액 100,000원 ]
		int initialBalance = strategy.getBalance(testAccountId, WITHDRAW_ONLY);
		assertEquals(100_000, initialBalance);

		int threadCount    = 2;
		int withdrawAmount = 50_000;

		ExecutorService executor   = Executors.newFixedThreadPool(threadCount);
		CountDownLatch readyLatch  = new CountDownLatch(threadCount);
		CountDownLatch startLatch  = new CountDownLatch(1);
		CountDownLatch doneLatch   = new CountDownLatch(threadCount);

		// [ When: 두 스레드가 동시에 인출을 시도할 때 ]
		for (int i = 0; i < threadCount; i++) {
			final int threadNumber = i + 1;
			executor.submit(() -> {
				readyLatch.countDown();
				try {
					startLatch.await();

					System.out.println("getClass : " + strategy.getClass());

					int balanceRead = strategy.getBalance(testAccountId, WITHDRAW_ONLY);
					System.out.printf("Thread-%d 읽은 잔고: %d원%n", threadNumber, balanceRead);

					strategy
						.withdraw(testAccountId, withdrawAmount, WITHDRAW_ONLY);

					int balanceAfter = strategy.getBalance(testAccountId, WITHDRAW_ONLY);
					System.out.printf("Thread-%d 인출 후 잔고: %d원%n", threadNumber, balanceAfter);

				} catch (InterruptedException ignored) {
				} finally {
					doneLatch.countDown();
				}
			});
		}

		readyLatch.await();
		startLatch.countDown();
		doneLatch.await();
		executor.shutdown();

		// [ Then: 최종 잔액이 0원이면 통과]
		int finalBalance = strategy.getBalance(testAccountId, WITHDRAW_ONLY);
		System.out.printf("초기 잔액=%d, 최종 잔액=%d%n", initialBalance, finalBalance);
		assertEquals(0, finalBalance);

	}

	@Test
	@DisplayName("Reentrant Vanilla - 잔고가 100,000원인 계좌에서 50개의 스레드가 동시에 1,00원씩 인출")
	void oneHundredThreadWithdraw() throws InterruptedException {
		// [ Given: 초기 잔액 100,000원 ]
		int initialBalance = strategy.getBalance(testAccountId, WITHDRAW_ONLY);
		assertEquals(100_000, initialBalance);

		int threadCount    = 50;
		int withdrawAmount = 1_000;

		ExecutorService executor   = Executors.newFixedThreadPool(threadCount);
		CountDownLatch readyLatch  = new CountDownLatch(threadCount);
		CountDownLatch startLatch  = new CountDownLatch(1);
		CountDownLatch doneLatch   = new CountDownLatch(threadCount);

		// [ When: 50개의 스레드가 동시에 인출을 시도할 때 ]
		for (int i = 0; i < threadCount; i++) {
			final int threadNumber = i + 1;
			executor.submit(() -> {
				readyLatch.countDown();
				try {
					startLatch.await();

					int balanceRead = strategy.getBalance(testAccountId, WITHDRAW_ONLY);
					System.out.printf("Thread-%d 읽은 잔고: %d원%n", threadNumber, balanceRead);

					strategy.withdraw(testAccountId, withdrawAmount, WITHDRAW_ONLY);

					int balanceAfter = strategy.getBalance(testAccountId, WITHDRAW_ONLY);
					System.out.printf("Thread-%d 인출 후 잔고: %d원%n", threadNumber, balanceAfter);

				} catch (InterruptedException ignored) {
				} finally {
					doneLatch.countDown();
				}
			});
		}

		readyLatch.await();
		startLatch.countDown();
		doneLatch.await();
		executor.shutdown();

		// [ Then: 최종 잔액이 50,00원이면 통과 ]
		int finalBalance = strategy.getBalance(testAccountId, WITHDRAW_ONLY);
		System.out.printf("초기 잔액=%d, 최종 잔액=%d%n", initialBalance, finalBalance);

		assertEquals(50_000, finalBalance);
	}
}