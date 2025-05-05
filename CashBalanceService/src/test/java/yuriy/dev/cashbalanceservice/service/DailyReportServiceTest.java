package yuriy.dev.cashbalanceservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yuriy.dev.cashbalanceservice.dto.ExchangeRateDto;
import yuriy.dev.cashbalanceservice.exception.NotFoundException;
import yuriy.dev.cashbalanceservice.model.CashBalance;
import yuriy.dev.cashbalanceservice.model.Currency;
import yuriy.dev.cashbalanceservice.model.DailyReport;
import yuriy.dev.cashbalanceservice.repository.CashBalanceRepository;
import yuriy.dev.cashbalanceservice.repository.CurrencyRepository;
import yuriy.dev.cashbalanceservice.repository.DailyReportRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DailyReportServiceTest {

    @Mock
    private CashBalanceRepository cashBalanceRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private DailyReportRepository dailyReportRepository;

    @InjectMocks
    private DailyReportService dailyReportService;

    private Currency usdCurrency;
    private Currency eurCurrency;
    private CashBalance eurCashBalance;
    private DailyReport dailyReport;

    @BeforeEach
    void setUp() {
        usdCurrency = Currency.builder()
                .id(UUID.randomUUID())
                .code("USD")
                .name("Dollar")
                .build();
        eurCurrency = Currency.builder()
                .id(UUID.randomUUID())
                .code("EUR")
                .name("Euro")
                .build();

        CashBalance usdCashBalance = CashBalance.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1000))
                .currency(usdCurrency)
                .build();
        eurCashBalance = CashBalance.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(850))
                .currency(eurCurrency)
                .build();
        dailyReport = DailyReport.builder()
                .id(UUID.randomUUID())
                .totalInBaseCurrency(BigDecimal.valueOf(10000))
                .date(LocalDate.now())
                .build();
    }


    @Test
    void processDailyReportUpdate_BaseCurrencyIsUSD_UpdatesCorrectly() {
        ExchangeRateDto rateDto = new ExchangeRateDto(UUID.randomUUID(),
                "USD", "EUR",
                BigDecimal.valueOf(0.85),
                BigDecimal.valueOf(0.90),
                LocalDate.now()
        );

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eurCurrency));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(eurCurrency.getId()))
                .thenReturn(Optional.of(eurCashBalance));
        when(dailyReportRepository.findAll()).thenReturn(List.of(dailyReport));

        dailyReportService.processDailyReportUpdate(rateDto);

        BigDecimal expectedDifference = BigDecimal.valueOf(850).divide(BigDecimal.valueOf(0.90), 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.valueOf(850).divide(BigDecimal.valueOf(0.85), 6, RoundingMode.HALF_UP));

        assertThat(dailyReport.getTotalInBaseCurrency())
                .isEqualByComparingTo(BigDecimal.valueOf(10000).add(expectedDifference));

        verify(dailyReportRepository).save(dailyReport);
    }

    @Test
    void processDailyReportUpdate_TargetCurrencyIsUSD_UpdatesCorrectly() {
        ExchangeRateDto rateDto = new ExchangeRateDto(
                UUID.randomUUID(),
                "EUR", "USD",
                BigDecimal.valueOf(1.18),
                BigDecimal.valueOf(1.20),
                LocalDate.now()
        );

        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eurCurrency));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(eurCurrency.getId()))
                .thenReturn(Optional.of(eurCashBalance));
        when(dailyReportRepository.findAll()).thenReturn(List.of(dailyReport));

        dailyReportService.processDailyReportUpdate(rateDto);

        BigDecimal expectedDifference = BigDecimal.valueOf(850).multiply(BigDecimal.valueOf(1.20))
                .subtract(BigDecimal.valueOf(850).multiply(BigDecimal.valueOf(1.18)));

        assertThat(dailyReport.getTotalInBaseCurrency())
                .isEqualByComparingTo(BigDecimal.valueOf(10000).add(expectedDifference));
    }

    @Test
    void processDailyReportUpdate_CurrencyNotFound_ThrowsException() {
        ExchangeRateDto rateDto = new ExchangeRateDto(UUID.randomUUID(), "USD", "EUR", BigDecimal.ONE, BigDecimal.ONE, LocalDate.now());
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> dailyReportService.processDailyReportUpdate(rateDto));
    }

    @Test
    void processDailyReportUpdate_CashBalanceNotFound_ThrowsException() {
        ExchangeRateDto rateDto = new ExchangeRateDto(UUID.randomUUID(), "USD", "EUR", BigDecimal.ONE, BigDecimal.ONE, LocalDate.now());
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eurCurrency));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> dailyReportService.processDailyReportUpdate(rateDto));
    }

    @Test
    void processDailyReportUpdate_NoDailyReport_ThrowsException() {
        ExchangeRateDto rateDto = new ExchangeRateDto(UUID.randomUUID(), "USD", "EUR", BigDecimal.ONE, BigDecimal.ONE, LocalDate.now());
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eurCurrency));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(any())).thenReturn(Optional.of(eurCashBalance));
        when(dailyReportRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
                () -> dailyReportService.processDailyReportUpdate(rateDto));
    }


}
