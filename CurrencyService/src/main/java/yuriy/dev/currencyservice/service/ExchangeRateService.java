package yuriy.dev.currencyservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yuriy.dev.currencyservice.dto.ExchangeRateDto;
import yuriy.dev.currencyservice.mapper.ExchangeRateMapper;
import yuriy.dev.currencyservice.model.ExchangeRate;
import yuriy.dev.currencyservice.repository.ExchangeRateRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    private final ExchangeRateMapper exchangeRateMapper;

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

    public ExchangeRateDto addExchangeRate(ExchangeRateDto exchangeRateDto){
        ExchangeRate exchangeRate = exchangeRateMapper.toExchangeRate(exchangeRateDto);
        return exchangeRateMapper.toExchangeRateDto(exchangeRateRepository.save(exchangeRate));
    }

    public ExchangeRateDto updateExchangeRate(UUID id, ExchangeRateDto exchangeRateDto){
        ExchangeRate exchangeRate = exchangeRateRepository.findById(id).orElse(null);
        if(exchangeRate != null){
            exchangeRate.setBaseCurrency(exchangeRate.getBaseCurrency());
            exchangeRate.setTargetCurrency(exchangeRate.getTargetCurrency());
            exchangeRate.setRate(exchangeRateDto.rate());
            exchangeRate.setDate(exchangeRateDto.date());
            return exchangeRateMapper.toExchangeRateDto(exchangeRateRepository.save(exchangeRate));
        }
        return null;
    }

    public void deleteExchangeRate(UUID id){
        exchangeRateRepository.deleteById(id);
    }


}
