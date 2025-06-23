package ru.pionerpixel.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Данные запроса на вход")
public class LoginRequestDto {
    @NotBlank(message = "Email или телефон не могут быть пустыми")
    @Schema(description = "Email или номер телефона пользователя", example = "user@example.com или 79201234567")
    private String emailOrPhone;

    @NotBlank(message = "Пароль не может быть пустым")
    @Schema(description = "Пароль пользователя", example = "securePassword123")
    private String password;

    // Геттеры и сеттеры
    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public void setEmailOrPhone(String emailOrPhone) {
        this.emailOrPhone = emailOrPhone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}