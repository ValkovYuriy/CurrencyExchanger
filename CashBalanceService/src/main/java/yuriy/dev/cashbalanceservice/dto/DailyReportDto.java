package yuriy.dev.cashbalanceservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DailyReportDto(
        UUID id,
        BigDecimal totalInBaseCurrency,
        LocalDate date
) {
}
