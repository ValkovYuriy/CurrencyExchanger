package yuriy.dev.currencyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateExchangeRateDto(
        @Schema(description = "Курс обмена")
        @NotNull(message = "Курс обмена должен быть инициализирован")
        @Positive(message = "Курс обмена должен быть положительным")
        BigDecimal rate
) {
}
