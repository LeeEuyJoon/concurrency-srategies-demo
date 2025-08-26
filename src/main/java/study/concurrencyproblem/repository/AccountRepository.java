package study.concurrencyproblem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import study.concurrencyproblem.domain.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findById(Long id);

	// 예금
	@Modifying
	@Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.id = :id")
	Integer deposit(@Param("id") Long id, @Param("amount") Integer amount);

	// 출금
	@Modifying
	@Query("UPDATE Account a SET a.balance = a.balance - :amount WHERE a.id = :id")
	Integer withdraw(@Param("id") Long id, @Param("amount") Integer amount);

	// 잔액 조회
	@Query("SELECT a.balance FROM Account a WHERE a.id = :id")
	Optional<Integer> getBalance(@Param("id") Long id);

	// 조회 - Pessimistic Lock
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM Account a WHERE a.id = :id")
	Optional<Account> findByIdWithPessimisticLock(@Param("id") Long id);

	// 조회 - Optimistic Lock
	@Lock(LockModeType.OPTIMISTIC)
	@Query("SELECT a FROM Account a WHERE a.id = :id")
	Optional<Account> findByIdWithOptimisticLock(@Param("id") Long id);

	// 락 획득 - Named Lock (성공=1, 타임아웃=0, 오류=NULL)
	@Query(value = "SELECT GET_LOCK(:key, :timeoutSec)", nativeQuery = true)
	Integer getLock(@Param("key") String key, @Param("timeoutSec") int timeoutSec);

	// 락 해제 - Named Lock (성공=1, 내가 주인 아님=0, 없음=NULL)
	@Query(value = "SELECT RELEASE_LOCK(:key)", nativeQuery = true)
	Integer releaseLock(@Param("key") String key);

} 