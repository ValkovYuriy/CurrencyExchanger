package yuriy.dev.cashbalanceservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import yuriy.dev.cashbalanceservice.dto.DealDto;
import yuriy.dev.cashbalanceservice.dto.ExchangeRateDto;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KafkaService {

    private final CashBalanceService cashBalanceService;
    private final DailyReportService dailyReportService;

    @KafkaListener(topics = "update-currency-balance")
    public void updateCashBalance(ConsumerRecord<String, DealDto> record) {
        log.info("Получена запись: {}", record);
        DealDto dealDetails = record.value();
        try {
            cashBalanceService.processCurrencyBalanceUpdate(dealDetails);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", e.getMessage());
        }
    }


    @KafkaListener(topics = "update-total-balance")
    public void updateDailyReport(ConsumerRecord<String, ExchangeRateDto> record) {
        log.info("Получена запись: {}", record);
        ExchangeRateDto exchangeRateDetails = record.value();
        try {
            dailyReportService.processDailyReportUpdate(exchangeRateDetails);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", e.getMessage());
        }
    }


}
