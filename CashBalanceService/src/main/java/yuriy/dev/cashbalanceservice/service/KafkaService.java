package yuriy.dev.cashbalanceservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import yuriy.dev.cashbalanceservice.dto.DealDto;
import yuriy.dev.cashbalanceservice.mapper.CashBalanceMapper;
import yuriy.dev.cashbalanceservice.model.CashBalance;
import yuriy.dev.cashbalanceservice.model.Currency;
import yuriy.dev.cashbalanceservice.repository.CashBalanceRepository;
import yuriy.dev.cashbalanceservice.repository.CurrencyRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KafkaService {

    private final CashBalanceService cashBalanceService;
    private final CashBalanceRepository cashBalanceRepository;
    private final CurrencyRepository currencyRepository;
    private final CashBalanceMapper cashBalanceMapper;

    @KafkaListener(topics = "update-currency-balance")
    public void updateCashBalance(ConsumerRecord<String, DealDto> record) {
        log.info("Получена запись: {}", record);
        DealDto dealDetails = record.value();
        try {
            processCurrencyBalanceUpdate(dealDetails);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", e.getMessage());
        }
    }

    private void processCurrencyBalanceUpdate(DealDto dealDetails) {
        Currency fromCurrency = getCurrencyOrThrow(dealDetails.fromCurrencyCode());
        Currency toCurrency = getCurrencyOrThrow(dealDetails.toCurrencyCode());
        updateCurrencyBalance(fromCurrency, dealDetails.amountFrom(),true);
        updateCurrencyBalance(toCurrency, dealDetails.amountTo(),false);
    }

    private Currency getCurrencyOrThrow(String currencyCode) {
        return currencyRepository.findByCode(currencyCode)
                .orElseThrow(() -> new RuntimeException("Валюта не найдена: " + currencyCode));
    }


    private void updateCurrencyBalance(Currency currency, BigDecimal amount, boolean isSourceCurrency) {
        CashBalance cashBalance = cashBalanceRepository.findCashBalanceByCurrencyId(currency.getId())
                .orElseThrow(() ->
                        new RuntimeException("Баланс не найден для валюты: " + currency.getCode())
                );
        cashBalance.setAmount(isSourceCurrency ? cashBalance.getAmount().add(amount) : cashBalance.getAmount().subtract(amount));
        if(!isSourceCurrency && cashBalance.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Баланс не может быть отрицательным");
        }
        cashBalanceService.updateCashBalance(cashBalance.getId(), cashBalanceMapper.toCashBalanceDto(cashBalance));
        log.info("Обновлен баланс для валюты {} ", currency.getCode());
    }

}
