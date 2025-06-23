package ru.pionerpixel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pionerpixel.entity.User;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Добавляем новые методы
    default Optional<User> findByEmailOrPhone(String emailOrPhone) {
        Optional<User> byEmail = findByEmailsEmail(emailOrPhone);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        return findByPhonesPhone(emailOrPhone);
    }

    @Query("SELECT u FROM User u JOIN u.emails e WHERE e.email = :email")
    Optional<User> findByEmailsEmail(@Param("email") String email);

    @Query("SELECT u FROM User u JOIN u.phones p WHERE p.phone = :phone")
    Optional<User> findByPhonesPhone(@Param("phone") String phone);

    @Query("SELECT DISTINCT u FROM User u " +
        "LEFT JOIN u.emails e " +
        "LEFT JOIN u.phones p " +
        "WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT(:name, '%'))) " +
        "AND (:email IS NULL OR e.email = :email) " +
        "AND (:phone IS NULL OR p.phone = :phone) " +
        "AND (:dateOfBirth IS NULL OR u.dateOfBirth > :dateOfBirth)")
    Page<User> searchUsers(
        @Param("name") String name,
        @Param("email") String email,
        @Param("phone") String phone,
        @Param("dateOfBirth") LocalDate dateOfBirth,
        Pageable pageable
    );

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EmailData e WHERE e.email = :email")
    boolean existsByEmail(@Param("email") String email);

}