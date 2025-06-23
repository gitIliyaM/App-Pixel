package ru.pionerpixel.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на обновление токена")
public class RefreshTokenRequestDto {
    @NotBlank(message = "Refresh-токен не может быть пустым")
    @Schema(description = "Refresh-токен для получения нового access-токена", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ.....")
    private String refreshToken;

    // Геттеры и сеттеры
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}