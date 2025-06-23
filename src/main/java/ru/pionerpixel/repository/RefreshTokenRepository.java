package ru.pionerpixel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pionerpixel.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}