package yuriy.dev.cashbalanceservice.dto;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record DealDto(
        UUID id,
        UUID userId,
        String fromCurrencyCode,
        String toCurrencyCode,
        BigDecimal amountFrom,
        BigDecimal amountTo,
        BigDecimal exchangeRate,
        LocalDateTime timestamp
) {
}
