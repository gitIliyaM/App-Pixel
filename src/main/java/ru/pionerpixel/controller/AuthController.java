package ru.pionerpixel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.pionerpixel.dto.auth.AuthResponseDto;
import ru.pionerpixel.dto.login.LoginRequestDto;
import ru.pionerpixel.dto.token.RefreshTokenRequestDto;
import ru.pionerpixel.exception.RefreshTokenException;
import ru.pionerpixel.exception.UserNotFoundException;
import ru.pionerpixel.service.AuthService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Эндпоинты для аутентификации пользователей")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя", description = "Аутентификация пользователя и получение JWT токенов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
        @ApiResponse(responseCode = "400", description = "Неверные учетные данные или формат запроса"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Попытка входа для: {}", request.getEmailOrPhone());
        try {
            AuthResponseDto response = authService.login(
                request.getEmailOrPhone(),
                request.getPassword()
            );
            log.info("Сгенерированный access token: {}", response.getAccessToken());
            log.info("Сгенерированный refresh token: {}", response.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверные учетные данные");
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление токена", description = "Получение нового access token с помощью refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Токен успешно обновлен"),
        @ApiResponse(responseCode = "400", description = "Неверный или просроченный refresh token"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<AuthResponseDto> refreshToken(
        @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        log.info("Запрос на обновление токена");
        try {
            AuthResponseDto response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        } catch (RefreshTokenException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}