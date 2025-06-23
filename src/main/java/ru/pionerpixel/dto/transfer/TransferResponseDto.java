package ru.pionerpixel.dto.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "Ответ на операцию перевода денежных средств")
public class TransferResponseDto {
    @Schema(description = "Сообщение о статусе операции", example = "Перевод успешно выполнен")
    private String message;

    @Schema(description = "Временная метка операции")
    private LocalDateTime timestamp;
}