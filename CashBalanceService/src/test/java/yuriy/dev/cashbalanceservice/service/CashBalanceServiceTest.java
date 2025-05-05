package yuriy.dev.cashbalanceservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yuriy.dev.cashbalanceservice.dto.CashBalanceDto;
import yuriy.dev.cashbalanceservice.dto.DealDto;
import yuriy.dev.cashbalanceservice.exception.NegativeBalanceException;
import yuriy.dev.cashbalanceservice.exception.NotFoundException;
import yuriy.dev.cashbalanceservice.mapper.CashBalanceMapper;
import yuriy.dev.cashbalanceservice.model.CashBalance;
import yuriy.dev.cashbalanceservice.model.Currency;
import yuriy.dev.cashbalanceservice.repository.CashBalanceRepository;
import yuriy.dev.cashbalanceservice.repository.CurrencyRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CashBalanceServiceTest {

    @Mock
    private CashBalanceRepository cashBalanceRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CashBalanceMapper cashBalanceMapper;

    @InjectMocks
    private CashBalanceService cashBalanceService;

    private Currency usdCurrency;
    private Currency eurCurrency;
    private CashBalance usdCashBalance;
    private CashBalance eurCashBalance;
    private DealDto testDeal;

    @BeforeEach
    void setUp() {
        usdCurrency = new Currency(UUID.randomUUID(), "USD", "Dollar");
        eurCurrency = new Currency(UUID.randomUUID(), "EUR", "Euro");

        usdCashBalance = new CashBalance(UUID.randomUUID(), usdCurrency, BigDecimal.valueOf(1000),LocalDate.now());
        eurCashBalance = new CashBalance(UUID.randomUUID(), eurCurrency,BigDecimal.valueOf(850),LocalDate.now());

        testDeal = new DealDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "USD",
                "EUR",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(85),
                BigDecimal.valueOf(0.85),
                LocalDateTime.now()
        );
    }

    @Test
    void processCurrencyBalanceUpdate_ValidDeal_UpdatesBothBalances() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eurCurrency));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(usdCurrency.getId()))
                .thenReturn(Optional.of(usdCashBalance));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(eurCurrency.getId()))
                .thenReturn(Optional.of(eurCashBalance));
        when(cashBalanceRepository.findById(usdCashBalance.getId())).thenReturn(Optional.of(usdCashBalance));
        when(cashBalanceRepository.findById(eurCashBalance.getId())).thenReturn(Optional.of(eurCashBalance));
        when(cashBalanceMapper.toCashBalanceDto(any())).thenAnswer(inv -> {
            CashBalance cash = inv.getArgument(0);
            return CashBalanceDto.builder()
                    .id(cash.getId())
                    .currencyId(cash.getCurrency().getId())
                    .date(cash.getDate())
                    .amount(cash.getAmount())
                    .build();
        });

        cashBalanceService.processCurrencyBalanceUpdate(testDeal);


        assertThat(usdCashBalance.getAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(1100));

        assertThat(eurCashBalance.getAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(765));

        verify(cashBalanceRepository, times(2)).save(any(CashBalance.class));
    }

    @Test
    void processCurrencyBalanceUpdate_CurrencyNotFound_ThrowsException() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> cashBalanceService.processCurrencyBalanceUpdate(testDeal));
    }

    @Test
    void processCurrencyBalanceUpdate_CashBalanceNotFound_ThrowsException() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eurCurrency));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> cashBalanceService.processCurrencyBalanceUpdate(testDeal));
    }

    @Test
    void processCurrencyBalanceUpdate_NegativeBalance_ThrowsException() {
        eurCashBalance.setAmount(BigDecimal.valueOf(50));

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eurCurrency));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(usdCurrency.getId()))
                .thenReturn(Optional.of(usdCashBalance));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(eurCurrency.getId()))
                .thenReturn(Optional.of(eurCashBalance));

        assertThrows(NegativeBalanceException.class,
                () -> cashBalanceService.processCurrencyBalanceUpdate(testDeal));
    }

    @Test
    void processCurrencyBalanceUpdate_ZeroAmount_UpdatesCorrectly() {
        DealDto zeroDeal = new DealDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "USD",
                "EUR",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(0.85),
                LocalDateTime.now()
        );

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eurCurrency));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(usdCurrency.getId()))
                .thenReturn(Optional.of(usdCashBalance));
        when(cashBalanceRepository.findCashBalanceByCurrencyId(eurCurrency.getId()))
                .thenReturn(Optional.of(eurCashBalance));

        cashBalanceService.processCurrencyBalanceUpdate(zeroDeal);

        assertThat(usdCashBalance.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(eurCashBalance.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(850));
    }

    @Test
    void processCurrencyBalanceUpdate_NullDeal_ThrowsException() {
        assertThrows(NullPointerException.class,
                () -> cashBalanceService.processCurrencyBalanceUpdate(null));
    }

}
