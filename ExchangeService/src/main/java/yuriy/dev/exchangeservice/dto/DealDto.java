package yuriy.dev.exchangeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DealDto(
        @Schema(hidden = true)
        UUID id,
        @Schema(description = "Пользователь, которому принадлежит сделка")
        UserDto userDto,
        @Schema(description = "Код базовой валюты")
        String fromCurrencyCode,
        @Schema(description = "Код целевой валюты")
        String toCurrencyCode,
        @Schema(description = "Сумма в исходной валюте")
        BigDecimal amountFrom,
        @Schema(description = "Сумма в целевой валюте")
        BigDecimal amountTo,
        @Schema(description = "Курс обмена")
        BigDecimal exchangeRate,
        @Schema(description = "Время совершения сделки")
        LocalDateTime timestamp
) {
}
