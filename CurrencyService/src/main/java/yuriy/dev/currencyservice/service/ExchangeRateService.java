package yuriy.dev.currencyservice.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import yuriy.dev.currencyservice.dto.ExchangeRateDto;
import yuriy.dev.currencyservice.dto.ExchangeRateForKafka;
import yuriy.dev.currencyservice.dto.UpdateExchangeRateDto;
import yuriy.dev.currencyservice.mapper.ExchangeRateMapper;
import yuriy.dev.currencyservice.model.Currency;
import yuriy.dev.currencyservice.model.ExchangeRate;
import yuriy.dev.currencyservice.repository.CurrencyRepository;
import yuriy.dev.currencyservice.repository.ExchangeRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final KafkaTemplate<String, ExchangeRateForKafka> kafkaTemplate;

    private final ExchangeRateMapper exchangeRateMapper;
    private final CurrencyRepository currencyRepository;

    public List<ExchangeRateDto> findAllExchangeRates(){
        return exchangeRateRepository
                .findAll()
                .stream()
                .map(exchangeRateMapper::toExchangeRateDto)
                .toList();
    }

    public ExchangeRateDto findExchangeRateById(UUID id){
        return exchangeRateRepository.findById(id).map(exchangeRateMapper::toExchangeRateDto).orElse(null);
    }

    public ExchangeRateDto findExchangeRateForCurrencies(UUID baseCurrencyId, UUID targetCurrencyId, LocalDate date){
        return exchangeRateRepository
                .getExchangeRateForCurrencies(baseCurrencyId, targetCurrencyId, date)
                .map(exchangeRateMapper::toExchangeRateDto)
                .orElse(null);
    }

    @Transactional
    public ExchangeRateDto addExchangeRate(ExchangeRateDto exchangeRateDto){
        ExchangeRate exchangeRate = exchangeRateMapper.toExchangeRate(exchangeRateDto);
        exchangeRate.setDate(LocalDate.now());
        exchangeRate.setBaseCurrency(currencyRepository.findByCode(exchangeRateDto.baseCurrencyDto().code())
                .orElseGet(() -> currencyRepository.save(Currency.builder()
                        .code(exchangeRateDto.baseCurrencyDto().code())
                        .name(exchangeRateDto.baseCurrencyDto().name())
                        .build())));
        exchangeRate.setTargetCurrency(currencyRepository.findByCode(exchangeRateDto.targetCurrencyDto().code())
                .orElseGet(() -> currencyRepository.save(Currency.builder()
                        .code(exchangeRateDto.targetCurrencyDto().code())
                        .name(exchangeRateDto.targetCurrencyDto().name())
                        .build())));
        return exchangeRateMapper.toExchangeRateDto(exchangeRateRepository.save(exchangeRate));
    }

    @Transactional
    @SneakyThrows
    public ExchangeRateDto updateExchangeRate(UUID id, UpdateExchangeRateDto exchangeRateDto){
        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Курс обмена с id %s не найден",id)));
        BigDecimal oldRate = exchangeRate.getRate();
        exchangeRate.setRate(exchangeRateDto.rate());
        exchangeRate.setDate(LocalDate.now());
        ExchangeRate updatedExchangeRate = exchangeRateRepository.save(exchangeRate);
        log.info("Был обновлен курс для базовой валюты: {} на целевую {}", updatedExchangeRate.getBaseCurrency().getCode(), updatedExchangeRate.getTargetCurrency().getCode());
        ExchangeRateForKafka exchangeRateForKafka = ExchangeRateForKafka.builder()
                .id(updatedExchangeRate.getId())
                .baseCurrencyCode(updatedExchangeRate.getBaseCurrency().getCode())
                .targetCurrencyCode(updatedExchangeRate.getTargetCurrency().getCode())
                .oldRate(oldRate)
                .newRate(updatedExchangeRate.getRate())
                .date(updatedExchangeRate.getDate())
                .build();
        kafkaTemplate.sendDefault(exchangeRateForKafka).get();
        log.info("Было отправлено сообщение в kafka с измененным курсом обмена");
        Optional<ExchangeRate> reverseRateOpt = exchangeRateRepository
                .findByBaseCurrencyAndTargetCurrency(
                        exchangeRate.getTargetCurrency().getId(),
                        exchangeRate.getBaseCurrency().getId()
                );

        if (reverseRateOpt.isPresent()) {
            ExchangeRate reverseRate = reverseRateOpt.get();
            reverseRate.setRate(BigDecimal.ONE.divide(exchangeRateDto.rate(), 6, RoundingMode.HALF_UP));
            reverseRate.setDate(LocalDate.now());
            exchangeRateRepository.save(reverseRate);
            log.info("Был обновлен курс для базовой валюты: {} на целевую {}", reverseRate.getBaseCurrency().getCode(), reverseRate.getTargetCurrency().getCode());
        } else {
            ExchangeRate newReverseRate = new ExchangeRate();
            newReverseRate.setBaseCurrency(exchangeRate.getTargetCurrency());
            newReverseRate.setTargetCurrency(exchangeRate.getBaseCurrency());
            newReverseRate.setRate(BigDecimal.ONE.divide(exchangeRateDto.rate(), 6, RoundingMode.HALF_UP));
            newReverseRate.setDate(LocalDate.now());
            exchangeRateRepository.save(newReverseRate);
            log.info("Был сохранен курс для базовой валюты: {} на целевую {}", newReverseRate.getBaseCurrency().getCode(), newReverseRate.getTargetCurrency().getCode());
        }
        return exchangeRateMapper.toExchangeRateDto(updatedExchangeRate);
    }

    public void deleteExchangeRate(UUID id){
        exchangeRateRepository.deleteById(id);
    }


}
