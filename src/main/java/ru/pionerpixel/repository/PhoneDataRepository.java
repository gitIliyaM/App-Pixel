package ru.pionerpixel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pionerpixel.entity.PhoneData;

import java.util.List;

public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {
    boolean existsByPhone(String phone);

    @Modifying
    @Query("DELETE FROM PhoneData p WHERE p.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}