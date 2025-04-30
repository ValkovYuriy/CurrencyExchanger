package yuriy.dev.currencyservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExchangeRateForKafka {
    private UUID id;
    private String baseCurrencyCode;
    private String targetCurrencyCode;
    private BigDecimal oldRate;
    private BigDecimal newRate;
    private LocalDate date;
}
