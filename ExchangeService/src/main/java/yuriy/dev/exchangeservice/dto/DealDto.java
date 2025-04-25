package yuriy.dev.exchangeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DealDto(
        UUID id,
        @Schema(name = "Пользователь, которому принадлежит сделка")
        UserDto userDto,
        @Schema(name = "Код базовой валюты")
        String fromCurrencyCode,
        @Schema(name = "Код целевой валюты")
        String toCurrencyCode,
        @Schema(name = "Сумма в исходной валюте")
        BigDecimal amountFrom,
        @Schema(name = "Сумма в целевой валюте")
        BigDecimal amountTo,
        @Schema(name = "Курс обмена")
        BigDecimal exchangeRate,
        @Schema(name = "Время совершения сделки")
        LocalDateTime timestamp
) {
}
