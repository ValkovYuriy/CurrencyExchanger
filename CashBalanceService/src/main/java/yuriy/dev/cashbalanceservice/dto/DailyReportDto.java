package yuriy.dev.cashbalanceservice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record DailyReportDto(
        UUID id,
        BigDecimal totalInBaseCurrency,
        LocalDate date
) {
}
