package ru.pionerpixel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.pionerpixel.dto.token.RefreshTokenRequestDto;
import ru.pionerpixel.entity.RefreshToken;
import ru.pionerpixel.exception.LoginException;
import ru.pionerpixel.exception.RefreshTokenException;
import ru.pionerpixel.exception.UserNotFoundException;
import ru.pionerpixel.repository.RefreshTokenRepository;
import ru.pionerpixel.repository.UserRepository;
import ru.pionerpixel.entity.User;
import ru.pionerpixel.dto.auth.AuthResponseDto;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthResponseDto login(String emailOrPhone, String password) {
        User user = userRepository.findByEmailOrPhone(emailOrPhone)
            .orElseThrow(() -> new UserNotFoundException("Пользователь не найден с email/телефоном: " + emailOrPhone));

        log.info("Попытка входа для пользователя: {}", emailOrPhone);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("Ошибка аутентификации для пользователя: {}", emailOrPhone);
            throw new BadCredentialsException("Неверный email/телефон или пароль");
        }

        String accessToken = jwtService.generateAccessToken(user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDto(accessToken, refreshToken.getToken());
    }

    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        try {
            RefreshToken oldToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RefreshTokenException("Неверный refresh-токен"));
            refreshTokenService.verifyExpiration(oldToken);

            refreshTokenRepository.delete(oldToken);

            User user = oldToken.getUser();
            String newAccessToken = jwtService.generateAccessToken(user.getId());
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user); // Новый refresh-токен

            return new AuthResponseDto(newAccessToken, newRefreshToken.getToken());
        } catch (Exception e) {
            log.error("Ошибка обновления токена", e);
            throw new RefreshTokenException("Неверный или просроченный refresh-токен");
        }
    }
}