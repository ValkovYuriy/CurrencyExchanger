package yuriy.dev.cashbalanceservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CashBalanceDto(
        UUID id,
        UUID currencyId,
        BigDecimal amount,
        LocalDate date
) {
}
