package yuriy.dev.currencyservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import yuriy.dev.currencyservice.dto.ExchangeRateDto;
import yuriy.dev.currencyservice.model.ExchangeRate;

@Mapper(componentModel = "spring",uses = CurrencyMapper.class)
public interface ExchangeRateMapper {

    @Mapping(target = "baseCurrency",source = "baseCurrencyDto")
    @Mapping(target = "targetCurrency",source = "targetCurrencyDto")
    ExchangeRate toExchangeRate(ExchangeRateDto exchangeRateDto);

    @Mapping(target = "baseCurrencyDto",source = "baseCurrency")
    @Mapping(target = "targetCurrencyDto",source = "targetCurrency")
    ExchangeRateDto toExchangeRateDto(ExchangeRate exchangeRate);
}
