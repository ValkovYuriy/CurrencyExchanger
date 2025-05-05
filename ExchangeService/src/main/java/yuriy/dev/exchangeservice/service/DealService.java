package yuriy.dev.exchangeservice.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import yuriy.dev.exchangeservice.dto.CurrencyDto;
import yuriy.dev.exchangeservice.dto.DealDto;
import yuriy.dev.exchangeservice.dto.ExchangeRateDto;
import yuriy.dev.exchangeservice.dto.RequestDto;
import yuriy.dev.exchangeservice.exception.NotFoundException;
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
        User currentUser = securityUtils.getCurrentUser();
        return currentUser.getRoles().stream().anyMatch(role -> role.getRole().equals("ROLE_ADMIN"))
                ? dealRepository
                .findAll(pageable)
                .stream()
                .map(dealMapper::toDealDto)
                .toList()
                : dealRepository
                .findAllByUserId(currentUser.getId())
                .stream()
                .map(dealMapper::toDealDto)
                .toList();
    }

    public List<DealDto> findDealsBetweenDates(RequestDto requestDto, LocalDate fromDate, LocalDate toDate) {
        User currentUser = securityUtils.getCurrentUser();
        return currentUser.getRoles().stream().anyMatch(role -> role.getRole().equals("ROLE_ADMIN"))
                ? dealRepository.findDealsBetween(requestDto.getFrom(),requestDto.getSize(),fromDate,toDate)
                .stream()
                .map(dealMapper::toDealDto)
                .toList()
                : dealRepository.findDealsBetween(requestDto.getFrom(),requestDto.getSize(),fromDate,toDate)
                .stream()
                .filter(deal -> deal.getUser().getId().equals(currentUser.getId()))
                .map(dealMapper::toDealDto)
                .toList();
    }

    public DealDto findDealById(UUID id){
        DealDto dealDto = dealRepository.findById(id).map(dealMapper::toDealDto).orElseThrow(() -> new NotFoundException("Сделка не найдена"));
        User currentUser = securityUtils.getCurrentUser();
        if(!dealDto.userId().equals(currentUser.getId()) && currentUser.getRoles().stream().noneMatch(role -> role.getRole().equals("ROLE_ADMIN"))){
            throw new AccessDeniedException("Доступ запрещен");
        }
        return dealDto;
    }

    @Transactional
    @SneakyThrows
    public DealDto addDeal(DealDto dealDto) {
        CurrencyDto fromCurrency = currencyServiceClient.getCurrencyByCode(dealDto.fromCurrencyCode().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Валюта с кодом " + dealDto.fromCurrencyCode().toUpperCase() + " не найдена"));
        CurrencyDto toCurrency = currencyServiceClient.getCurrencyByCode(dealDto.toCurrencyCode().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Валюта с кодом " + dealDto.toCurrencyCode().toUpperCase() + " не найдена"));
        ExchangeRateDto exchangeRate = currencyServiceClient.getExchangeRate(fromCurrency.getId(),toCurrency.getId(), LocalDate.now())
                .orElseThrow(() -> new NotFoundException(String.format("Курс обмена для валюты %s на %s не найден", fromCurrency.getName(),toCurrency.getName())));

        Deal deal = dealMapper.toDeal(dealDto);
        User currentUser = securityUtils.getCurrentUser();
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
        Deal deal = dealRepository.findById(id).orElseThrow(()-> new NotFoundException("Сделка не найдена"));
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
