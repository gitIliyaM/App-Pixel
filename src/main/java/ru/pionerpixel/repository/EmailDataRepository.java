package ru.pionerpixel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pionerpixel.entity.EmailData;

import java.util.List;

public interface EmailDataRepository extends JpaRepository<EmailData, Long> {
    boolean existsByEmail(String email);
    List<EmailData> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM EmailData e WHERE e.id = :id AND e.user.id = :userId")
    int deleteUserEmail(@Param("userId") Long userId, @Param("id") Long emailId);

    @Modifying
    @Query("DELETE FROM EmailData e WHERE e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}