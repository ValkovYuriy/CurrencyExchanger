package yuriy.dev.cashbalanceservice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record CashBalanceDto(
        UUID id,
        UUID currencyId,
        BigDecimal amount,
        LocalDate date
) {
}
