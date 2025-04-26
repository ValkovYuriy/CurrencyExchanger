package yuriy.dev.currencyservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yuriy.dev.currencyservice.dto.CurrencyDto;
import yuriy.dev.currencyservice.mapper.CurrencyMapper;
import yuriy.dev.currencyservice.model.Currency;
import yuriy.dev.currencyservice.repository.CurrencyRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    private final CurrencyMapper currencyMapper;

    public List<CurrencyDto> findAllCurrencies(){
        return currencyRepository
                .findAll()
                .stream()
                .map(currencyMapper::toCurrencyDto)
                .toList();
    }

    public CurrencyDto findCurrencyById(UUID id){
        return currencyRepository.findById(id).map(currencyMapper::toCurrencyDto).orElse(null);
    }

    public CurrencyDto findCurrencyByCode(String code){
        return currencyRepository.findByCode(code).map(currencyMapper::toCurrencyDto).orElse(null);
    }

    public CurrencyDto addCurrency(CurrencyDto currencyDto){
        Currency currency = currencyMapper.toCurrency(currencyDto);
        return currencyMapper.toCurrencyDto(currencyRepository.save(currency));
    }

    public CurrencyDto updateCurrency(UUID id, CurrencyDto currencyDto){
        Currency currency = currencyRepository.findById(id).orElse(null);
        if(currency != null){
            currency.setCode(currencyDto.code());
            currency.setName(currencyDto.name());
            return currencyMapper.toCurrencyDto(currencyRepository.save(currency));
        }
        return null;
    }

    public void deleteCurrency(UUID id){
        currencyRepository.deleteById(id);
    }


}
