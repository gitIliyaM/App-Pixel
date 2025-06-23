package ru.pionerpixel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pionerpixel.dto.user.UserResponseDto;
import ru.pionerpixel.dto.user.UserUpdateDto;
import ru.pionerpixel.entity.Account;
import ru.pionerpixel.entity.EmailData;
import ru.pionerpixel.entity.PhoneData;
import ru.pionerpixel.entity.User;
import ru.pionerpixel.exception.ForbiddenOperationException;
import ru.pionerpixel.exception.InsufficientFundsException;
import ru.pionerpixel.exception.UserNotFoundException;
import ru.pionerpixel.mapper.UserMapper;
import ru.pionerpixel.repository.AccountRepository;
import ru.pionerpixel.repository.EmailDataRepository;
import ru.pionerpixel.repository.PhoneDataRepository;
import ru.pionerpixel.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<User> searchUsers(String name, String email, String phone, LocalDate dateOfBirth, Pageable pageable) {
        return userRepository.searchUsers(name, email, phone, dateOfBirth, pageable);
    }

    @Transactional
    public void addEmail(Long userId, String email) {
        if (emailDataRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        EmailData newEmail = new EmailData();
        newEmail.setUser(user);
        newEmail.setEmail(email);
        emailDataRepository.save(newEmail);
    }

    @Transactional
    public void deleteEmail(Long userId, Long emailId) {
        EmailData email = emailDataRepository.findById(emailId)
            .orElseThrow(() -> new IllegalArgumentException("Email not found"));
        if (!email.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException("You can only delete your own emails");
        }
        emailDataRepository.delete(email);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void addPhone(Long userId, String phone) {
        if (phoneDataRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone already exists");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        PhoneData newPhone = new PhoneData();
        newPhone.setUser(user);
        newPhone.setPhone(phone);
        phoneDataRepository.save(newPhone);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void updateEmails(Long userId, List<String> newEmails) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Проверяем уникальность новых email
        for (String email : newEmails) {
            if (emailDataRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists: " + email);
            }
        }

        // Удаляем старые email
        emailDataRepository.deleteByUserId(userId);

        // Добавляем новые
        for (String email : newEmails) {
            EmailData emailData = new EmailData();
            emailData.setUser(user);
            emailData.setEmail(email);
            emailDataRepository.save(emailData);
        }
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void updatePhones(Long userId, List<String> newPhones) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Проверка формата и уникальности телефонов
        for (String phone : newPhones) {
            if (!phone.matches("^7\\d{10}$")) {
                throw new IllegalArgumentException("Invalid phone format: " + phone);
            }
            if (phoneDataRepository.existsByPhone(phone)) {
                throw new IllegalArgumentException("Phone already exists: " + phone);
            }
        }

        // Удаляем старые телефоны
        phoneDataRepository.deleteByUserId(userId);

        // Добавляем новые
        for (String phone : newPhones) {
            PhoneData phoneData = new PhoneData();
            phoneData.setUser(user);
            phoneData.setPhone(phone);
            phoneDataRepository.save(phoneData);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return userMapper.toDto(user);
    }

    @Transactional
    public void updateUserData(Long userId, UserUpdateDto updateDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Проверка email
        if (updateDto.getEmails() != null) {
            for (String email : updateDto.getEmails()) {
                if (emailDataRepository.existsByEmail(email)) {
                    throw new IllegalArgumentException("Email already exists: " + email);
                }
            }
            updateEmails(userId, updateDto.getEmails());
        }

        // Проверка телефонов
        if (updateDto.getPhones() != null) {
            for (String phone : updateDto.getPhones()) {
                if (!phone.matches("^7\\d{10}$")) {
                    throw new IllegalArgumentException("Invalid phone format: " + phone);
                }
                if (phoneDataRepository.existsByPhone(phone)) {
                    throw new IllegalArgumentException("Phone already exists: " + phone);
                }
            }
            updatePhones(userId, updateDto.getPhones());
        }
    }

    @Transactional
    public void deletePhone(Long userId, Long phoneId) {
        PhoneData phone = phoneDataRepository.findById(phoneId)
            .orElseThrow(() -> new IllegalArgumentException("Phone not found"));

        if (!phone.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException("You can only delete your own phones");
        }

        phoneDataRepository.delete(phone);
    }
}