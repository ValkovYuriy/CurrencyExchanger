package yuriy.dev.cashbalanceservice.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yuriy.dev.cashbalanceservice.dto.ExchangeRateDto;
import yuriy.dev.cashbalanceservice.model.CashBalance;
import yuriy.dev.cashbalanceservice.model.Currency;
import yuriy.dev.cashbalanceservice.model.DailyReport;
import yuriy.dev.cashbalanceservice.repository.CashBalanceRepository;
import yuriy.dev.cashbalanceservice.repository.CurrencyRepository;
import yuriy.dev.cashbalanceservice.repository.DailyReportRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DailyReportService {
    
    private final CashBalanceRepository cashBalanceRepository;
    private final CurrencyRepository currencyRepository;
    private final DailyReportRepository dailyReportRepository;

    public void processDailyReportUpdate(ExchangeRateDto exchangeRateDetails) {
        Currency baseCurrency = getCurrencyOrThrow(exchangeRateDetails.baseCurrencyCode());
        Currency targetCurrency = getCurrencyOrThrow(exchangeRateDetails.targetCurrencyCode());
        if(baseCurrency.getCode().equals("USD")){
            CashBalance targetCashBalance = cashBalanceRepository.findCashBalanceByCurrencyId(targetCurrency.getId())
                    .orElseThrow(() -> new RuntimeException(String.format("Баланс для валюты %s не найден",targetCurrency.getCode())));
            BigDecimal oldTargetBalanceInUsd = targetCashBalance.getAmount().divide(exchangeRateDetails.oldRate(),6, RoundingMode.HALF_UP);
            BigDecimal newTargetBalanceInUsd = targetCashBalance.getAmount().divide(exchangeRateDetails.newRate(),6, RoundingMode.HALF_UP);
            BigDecimal difference = newTargetBalanceInUsd.subtract(oldTargetBalanceInUsd);
            updateDailyReport(difference);
        } else if (targetCurrency.getCode().equals("USD")) {
            CashBalance baseCashBalance = cashBalanceRepository.findCashBalanceByCurrencyId(baseCurrency.getId())
                    .orElseThrow(() -> new RuntimeException(String.format("Баланс для валюты %s не найден",baseCurrency.getCode())));
            BigDecimal oldBaseBalanceInUsd = baseCashBalance.getAmount().multiply(exchangeRateDetails.oldRate());
            BigDecimal newBaseBalanceInUsd = baseCashBalance.getAmount().multiply(exchangeRateDetails.newRate());
            BigDecimal difference = newBaseBalanceInUsd.subtract(oldBaseBalanceInUsd);
            updateDailyReport(difference);
        }
    }

    private Currency getCurrencyOrThrow(String currencyCode) {
        return currencyRepository.findByCode(currencyCode)
                .orElseThrow(() -> new RuntimeException("Валюта не найдена: " + currencyCode));
    }

    private void updateDailyReport(BigDecimal difference) {
        DailyReport dailyReport = dailyReportRepository.findAll()
                .stream().findFirst().orElseThrow(() -> new RuntimeException("Нет данных о балансе в базовой валюте"));
        dailyReport.setTotalInBaseCurrency(dailyReport.getTotalInBaseCurrency().add(difference));
        dailyReportRepository.save(dailyReport);
        log.info("Был обновлен общий баланс в базовой валюте");
    }
}
