package yuriy.dev.exchangeservice.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import yuriy.dev.exchangeservice.dto.CurrencyDto;
import yuriy.dev.exchangeservice.dto.DealDto;
import yuriy.dev.exchangeservice.dto.ExchangeRateDto;
import yuriy.dev.exchangeservice.dto.RequestDto;
import yuriy.dev.exchangeservice.mapper.DealMapper;
import yuriy.dev.exchangeservice.model.Deal;
import yuriy.dev.exchangeservice.repository.DealRepository;
import yuriy.dev.model.User;
import yuriy.dev.util.SecurityUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DealService {

    private final CurrencyServiceClient currencyServiceClient;
    private final SecurityUtils securityUtils;

    private final KafkaTemplate<String, DealDto> kafkaTemplate;

    private final DealRepository dealRepository;

    private final DealMapper dealMapper;

    public List<DealDto> findAllDeals(RequestDto requestDto){
        Pageable pageable = PageRequest.of(requestDto.getFrom(), requestDto.getSize());
        return dealRepository
                .findAll(pageable)
                .stream()
                .map(dealMapper::toDealDto)
                .toList();
    }

    public List<DealDto> findDealsBetweenDates(RequestDto requestDto, LocalDate fromDate, LocalDate toDate) {
        return dealRepository.findDealsBetween(requestDto.getFrom(),requestDto.getSize(),fromDate,toDate)
                .stream()
                .map(dealMapper::toDealDto)
                .toList();
    }

    public DealDto findDealById(UUID id){
        return dealRepository.findById(id).map(dealMapper::toDealDto).orElse(null);
    }

    @Transactional
    @SneakyThrows
    public DealDto addDeal(DealDto dealDto) {
        CurrencyDto fromCurrency = currencyServiceClient.getCurrencyByCode(dealDto.fromCurrencyCode().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Валюта с кодом " + dealDto.fromCurrencyCode().toUpperCase() + " не найдена"));
        CurrencyDto toCurrency = currencyServiceClient.getCurrencyByCode(dealDto.toCurrencyCode().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Валюта с кодом " + dealDto.toCurrencyCode().toUpperCase() + " не найдена"));
        ExchangeRateDto exchangeRate = currencyServiceClient.getExchangeRate(fromCurrency.getId(),toCurrency.getId(), LocalDate.now())
                .orElseThrow(() -> new RuntimeException(String.format("Курс обмена для валюты %s на %s не найден", fromCurrency.getName(),toCurrency.getName())));

        Deal deal = dealMapper.toDeal(dealDto);
        User currentUser = securityUtils.getCurrentUser();
        if(!currentUser.getId().equals(dealDto.userId())){
            throw new RuntimeException("Id аутентифицированного пользователя не совпадает с указанным");
        }
        deal.setUser(currentUser);
        deal.setAmountTo(dealDto.amountFrom().multiply(exchangeRate.getRate()));
        deal.setExchangeRate(exchangeRate.getRate());
        deal.setTimestamp(LocalDateTime.now());
        DealDto savedDeal = dealMapper.toDealDto(dealRepository.save(deal));
        log.info("Сделка с id {} была сохранена", savedDeal.id());
        kafkaTemplate.sendDefault(savedDeal).get();
        log.info("Сделка с id {} была отправлена в kafka", savedDeal.id());
        return savedDeal;
    }

    @Transactional
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
