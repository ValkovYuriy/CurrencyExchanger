package yuriy.dev.exchangeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExchangeRateDto {

    private UUID id;

    private UUID baseCurrencyId;

    private UUID targetCurrencyId;

    private BigDecimal rate;

    private LocalDate date;
}
