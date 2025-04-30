package yuriy.dev.cashbalanceservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExchangeRateDto(
        UUID id,
        String baseCurrencyCode,
        String targetCurrencyCode,
        BigDecimal oldRate,
        BigDecimal newRate,
        LocalDate date
) {
}
