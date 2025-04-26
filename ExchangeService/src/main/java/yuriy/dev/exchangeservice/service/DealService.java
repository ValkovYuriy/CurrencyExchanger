package yuriy.dev.exchangeservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yuriy.dev.exchangeservice.dto.CurrencyDto;
import yuriy.dev.exchangeservice.dto.DealDto;
import yuriy.dev.exchangeservice.dto.ExchangeRateDto;
import yuriy.dev.exchangeservice.mapper.DealMapper;
import yuriy.dev.exchangeservice.model.Deal;
import yuriy.dev.exchangeservice.repository.DealRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DealService {

    private final CurrencyServiceClient currencyServiceClient;

    private final DealRepository dealRepository;

    private final DealMapper dealMapper;

    public List<DealDto> findAllDeals(){
        return dealRepository
                .findAll()
                .stream()
                .map(dealMapper::toDealDto)
                .toList();
    }

    public DealDto findDealById(UUID id){
        return dealRepository.findById(id).map(dealMapper::toDealDto).orElse(null);
    }

    public DealDto addDeal(DealDto dealDto){
        CurrencyDto fromCurrency = currencyServiceClient.getCurrencyByCode(dealDto.fromCurrencyCode())
                .orElseThrow(() -> new RuntimeException("Валюта с кодом " + dealDto.fromCurrencyCode() + "не найдена"));
        CurrencyDto toCurrency = currencyServiceClient.getCurrencyByCode(dealDto.toCurrencyCode())
                .orElseThrow(() -> new RuntimeException("Валюта с кодом " + dealDto.toCurrencyCode() + "не найдена"));
        ExchangeRateDto exchangeRate = currencyServiceClient.getExchangeRate(fromCurrency.getId(),toCurrency.getId(), LocalDate.now())
                .orElseThrow(() -> new RuntimeException(String.format("Курс обмена для валюты %s на %s не найден", fromCurrency.getName(),toCurrency.getName())));
        Deal deal = dealMapper.toDeal(dealDto);
        deal.setExchangeRate(exchangeRate.getRate());
        deal.setAmountTo(deal.getAmountFrom().multiply(exchangeRate.getRate()));
        deal.setTimestamp(LocalDateTime.now());
        DealDto savedDeal = dealMapper.toDealDto(dealRepository.save(deal));
        log.info("Сделка с id {} была сохранена", savedDeal.id());
        return savedDeal;
    }

    public DealDto updateDeal(UUID id, DealDto dealDto){
        Deal deal = dealRepository.findById(id).orElse(null);
        assert deal != null;
        deal.setTimestamp(dealDto.timestamp());
        deal.setAmountFrom(dealDto.amountFrom());
        deal.setAmountTo(dealDto.amountTo());
        deal.setFromCurrencyCode(dealDto.fromCurrencyCode());
        deal.setToCurrencyCode(dealDto.toCurrencyCode());
        deal.setExchangeRate(dealDto.exchangeRate());
        return dealMapper.toDealDto(dealRepository.save(deal));
    }

    public void deleteDeal(UUID id){
        dealRepository.deleteById(id);
    }


}
