package yuriy.dev.currencyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExchangeRateDto(
        @Schema(hidden = true)
        UUID id,
        @Schema(description = "Базовая валюта")
        CurrencyDto baseCurrencyDto,
        @Schema(description = "Целевая валюта")
        CurrencyDto targetCurrencyDto,
        @Schema(description = "Курс обмена")
        @NotNull(message = "Курс обмена должен быть инициализирован")
        @Positive(message = "Курс обмена должен быть положительным")
        BigDecimal rate,
        @Schema(hidden = true)
        LocalDate date
) {
}
