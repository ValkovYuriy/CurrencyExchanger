package yuriy.dev.exchangeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record DealDto(
        @Schema(hidden = true)
        UUID id,
        @Schema(description = "Id пользователя, которому принадлежит сделка")
        @NotNull(message = "Id пользователя не может быть пустым")
        UUID userId,
        @Schema(description = "Код базовой валюты")
        @NotBlank(message = "Код базовой валюты не может быть пустым")
        String fromCurrencyCode,
        @Schema(description = "Код целевой валюты")
        @NotBlank(message = "Код целевой валюты не может быть пустым")
        String toCurrencyCode,
        @Schema(description = "Сумма в исходной валюте")
        @Positive
        BigDecimal amountFrom,
        @Schema(description = "Сумма в целевой валюте", hidden = true)
        BigDecimal amountTo,
        @Schema(description = "Курс обмена", hidden = true)
        BigDecimal exchangeRate,
        @Schema(description = "Время совершения сделки",hidden = true)
        LocalDateTime timestamp
) {
}
