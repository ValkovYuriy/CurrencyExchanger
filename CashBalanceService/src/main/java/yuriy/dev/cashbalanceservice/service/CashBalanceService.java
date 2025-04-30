package yuriy.dev.cashbalanceservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yuriy.dev.cashbalanceservice.dto.CashBalanceDto;
import yuriy.dev.cashbalanceservice.dto.DealDto;
import yuriy.dev.cashbalanceservice.mapper.CashBalanceMapper;
import yuriy.dev.cashbalanceservice.model.CashBalance;
import yuriy.dev.cashbalanceservice.model.Currency;
import yuriy.dev.cashbalanceservice.repository.CashBalanceRepository;
import yuriy.dev.cashbalanceservice.repository.CurrencyRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashBalanceService {

    private final CashBalanceRepository cashBalanceRepository;
    private final CurrencyRepository currencyRepository;
    private final CashBalanceMapper cashBalanceMapper;

    public void processCurrencyBalanceUpdate(DealDto dealDetails) {
        Currency fromCurrency = getCurrencyOrThrow(dealDetails.fromCurrencyCode());
        Currency toCurrency = getCurrencyOrThrow(dealDetails.toCurrencyCode());
        updateCurrencyBalance(fromCurrency, dealDetails.amountFrom(),true);
        updateCurrencyBalance(toCurrency, dealDetails.amountTo(),false);
    }

    private Currency getCurrencyOrThrow(String currencyCode) {
        return currencyRepository.findByCode(currencyCode)
                .orElseThrow(() -> new RuntimeException("Валюта не найдена: " + currencyCode));
    }

    protected void updateCurrencyBalance(Currency currency, BigDecimal amount, boolean isSourceCurrency) {
        CashBalance cashBalance = cashBalanceRepository.findCashBalanceByCurrencyId(currency.getId())
                .orElseThrow(() ->
                        new RuntimeException("Баланс не найден для валюты: " + currency.getCode())
                );
        cashBalance.setAmount(isSourceCurrency ? cashBalance.getAmount().add(amount) : cashBalance.getAmount().subtract(amount));
        if(!isSourceCurrency && cashBalance.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Баланс не может быть отрицательным");
        }
        updateCashBalance(cashBalance.getId(), cashBalanceMapper.toCashBalanceDto(cashBalance));
        log.info("Обновлен баланс для валюты {} ", currency.getCode());
    }

    public void updateCashBalance(UUID cashBalanceId,CashBalanceDto cashBalanceDto) {
        CashBalance cashBalance = cashBalanceRepository.findById(cashBalanceId).orElse(null);
        if (cashBalance != null) {
            cashBalance.setAmount(cashBalanceDto.amount());
            cashBalance.setDate(LocalDate.now());
            CashBalance savedBalance = cashBalanceRepository.save(cashBalance);
            log.info("Обновлен баланс: {}", savedBalance);
        }
    }

}
