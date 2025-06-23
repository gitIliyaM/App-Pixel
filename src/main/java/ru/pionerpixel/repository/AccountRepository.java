package ru.pionerpixel.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pionerpixel.entity.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount " +
        "WHERE a.user.id = :userId AND a.balance + :amount >= 0")
    int updateBalance(
        @Param("userId") Long userId,
        @Param("amount") BigDecimal amount
    );

    @Query("SELECT a FROM Account a ORDER BY a.id")
    List<Account> findAllWithPagination(Pageable pageable);
}