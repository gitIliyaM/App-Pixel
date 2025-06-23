package ru.pionerpixel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.pionerpixel.dto.transfer.TransferRequestDto;
import ru.pionerpixel.dto.transfer.TransferResponseDto;
import ru.pionerpixel.dto.user.UserEmailsUpdateDto;
import ru.pionerpixel.dto.user.UserResponseDto;
import ru.pionerpixel.dto.user.UserUpdateDto;
import ru.pionerpixel.exception.ForbiddenOperationException;
import ru.pionerpixel.mapper.UserMapper;
import ru.pionerpixel.service.AccountService;
import ru.pionerpixel.service.JwtService;
import ru.pionerpixel.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "Управление пользователями", description = "Эндпоинты для управления пользователями")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Поиск пользователей", description = "Поиск пользователей с фильтрами и пагинацией")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователи успешно найдены"),
        @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Page<UserResponseDto>> searchUsers(
        @RequestHeader("Authorization") String token,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
        Pageable pageable
    ) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный токен");
        }

        try {
            jwtService.validateToken(token.replace("Bearer ", ""));

            log.info("Поиск пользователей с фильтрами - имя: {}, email: {}, телефон: {}, дата рождения: {}",
                name, email, phone, dateOfBirth);

            Page<UserResponseDto> responseDtoPage = userService.searchUsers(
                name,
                email,
                phone,
                dateOfBirth,
                pageable
            ).map(userMapper::toDto);

            return ResponseEntity.ok(responseDtoPage);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный токен");
        }
    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод денег", description = "Перевод денег между счетами пользователей")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Перевод успешно завершен"),
        @ApiResponse(responseCode = "400", description = "Неверный запрос перевода или недостаточно средств"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Запрещено"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<TransferResponseDto> transferMoney(
        @RequestHeader("Authorization") String token,
        @RequestBody @Valid TransferRequestDto transferRequestDto
    ) {
        String cleanToken = token.replace("Bearer ", "");
        Long fromUserId = jwtService.extractUserId(cleanToken);

        accountService.transferMoney(
            fromUserId,
            transferRequestDto.getRecipientId(),
            transferRequestDto.getAmount()
        );

        return ResponseEntity.ok(new TransferResponseDto(
            "Перевод успешно завершен",
            LocalDateTime.now()
        ));
    }

    @PutMapping("/{userId}/emails")
    @Operation(summary = "Обновление email пользователя", description = "Замена всех email пользователя на новые")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email успешно обновлены"),
        @ApiResponse(responseCode = "400", description = "Неверный формат email или дубликаты"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Запрещенная операция"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<UserResponseDto> updateUserEmails(
        @RequestHeader("Authorization") String token,
        @PathVariable Long userId,
        @RequestBody @Valid UserEmailsUpdateDto request
    ) {
        Long currentUserId = jwtService.extractUserId(token.replace("Bearer ", ""));
        if (!currentUserId.equals(userId)) {
            throw new ForbiddenOperationException("Вы можете обновлять только свои данные");
        }

        userService.updateEmails(userId, request.getEmails());
        UserResponseDto updatedUser = userService.getUserById(userId);

        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Обновление данных пользователя", description = "Обновление email и телефонов пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Данные пользователя успешно обновлены"),
        @ApiResponse(responseCode = "400", description = "Неверный формат данных или дубликаты"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Запрещенная операция"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<UserResponseDto> updateUser(
        @RequestHeader("Authorization") String token,
        @PathVariable Long userId,
        @RequestBody @Valid UserUpdateDto updateDto
    ) {
        Long currentUserId = jwtService.extractUserId(token.replace("Bearer ", ""));
        if (!currentUserId.equals(userId)) {
            throw new ForbiddenOperationException("Вы можете обновлять только свои данные");
        }

        userService.updateUserData(userId, updateDto);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping("/{userId}/emails")
    @Operation(summary = "Добавление email пользователю", description = "Добавление нового email пользователю")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email успешно добавлен"),
        @ApiResponse(responseCode = "400", description = "Неверный формат email или дубликат"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Запрещенная операция"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<UserResponseDto> addEmail(
        @RequestHeader("Authorization") String token,
        @PathVariable Long userId,
        @RequestBody @Email String email
    ) {
        Long currentUserId = jwtService.extractUserId(token.replace("Bearer ", ""));
        if (!currentUserId.equals(userId)) {
            throw new ForbiddenOperationException("Вы можете обновлять только свои данные");
        }

        userService.addEmail(userId, email);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @DeleteMapping("/{userId}/emails/{emailId}")
    @Operation(summary = "Удаление email пользователя", description = "Удаление конкретного email пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Email успешно удален"),
        @ApiResponse(responseCode = "400", description = "Неверный ID email"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Запрещенная операция"),
        @ApiResponse(responseCode = "404", description = "Пользователь или email не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Void> deleteEmail(
        @RequestHeader("Authorization") String token,
        @PathVariable Long userId,
        @PathVariable Long emailId
    ) {
        Long currentUserId = jwtService.extractUserId(token.replace("Bearer ", ""));
        if (!currentUserId.equals(userId)) {
            throw new ForbiddenOperationException("Вы можете обновлять только свои данные");
        }

        userService.deleteEmail(userId, emailId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/phones")
    @Operation(summary = "Добавление телефона пользователю", description = "Добавление нового номера телефона пользователю")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Телефон успешно добавлен"),
        @ApiResponse(responseCode = "400", description = "Неверный формат телефона или дубликат"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Запрещенная операция"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<UserResponseDto> addPhone(
        @RequestHeader("Authorization") String token,
        @PathVariable Long userId,
        @RequestBody @Pattern(regexp = "^7\\d{10}$") String phone
    ) {
        Long currentUserId = jwtService.extractUserId(token.replace("Bearer ", ""));
        if (!currentUserId.equals(userId)) {
            throw new ForbiddenOperationException("Вы можете обновлять только свои данные");
        }

        userService.addPhone(userId, phone);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @DeleteMapping("/{userId}/phones/{phoneId}")
    @Operation(summary = "Удаление телефона пользователя", description = "Удаление конкретного номера телефона пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Телефон успешно удален"),
        @ApiResponse(responseCode = "400", description = "Неверный ID телефона"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Запрещенная операция"),
        @ApiResponse(responseCode = "404", description = "Пользователь или телефон не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Void> deletePhone(
        @RequestHeader("Authorization") String token,
        @PathVariable Long userId,
        @PathVariable Long phoneId
    ) {
        Long currentUserId = jwtService.extractUserId(token.replace("Bearer ", ""));
        if (!currentUserId.equals(userId)) {
            throw new ForbiddenOperationException("Вы можете обновлять только свои данные");
        }

        userService.deletePhone(userId, phoneId);
        return ResponseEntity.noContent().build();
    }
}