package yuriy.dev.currencyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExchangeRateDto(
        @Schema(hidden = true)
        UUID id,
        @Schema(description = "Базовая валюта" )
        CurrencyDto baseCurrencyDto,
        @Schema(description = "Целевая валюта")
        CurrencyDto targetCurrencyDto,
        @Schema(description = "Курс обмена")
        BigDecimal rate,
        @Schema(description = "Дата установления курса")
        LocalDate date
) {
}
