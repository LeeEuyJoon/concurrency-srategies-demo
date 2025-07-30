package study.concurrencyproblem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import study.concurrencyproblem.domain.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findById(Long id);

	// 예금
	@Modifying
	@Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.id = :id")
	Integer deposit(@Param("id") Integer id, @Param("amount") Integer amount);

	// 출금
	@Modifying
	@Query("UPDATE Account a SET a.balance = a.balance - :amount WHERE a.id = :id")
	Integer withdraw(@Param("id") Integer id, @Param("amount") Integer amount);

	// 잔액 조회
	@Query("SELECT a.balance FROM Account a WHERE a.id = :id")
	Optional<Integer> getBalance(@Param("id") Long id);
} 