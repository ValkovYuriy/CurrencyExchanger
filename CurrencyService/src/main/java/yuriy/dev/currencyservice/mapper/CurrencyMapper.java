package yuriy.dev.currencyservice.mapper;

import org.mapstruct.Mapper;
import yuriy.dev.currencyservice.dto.CurrencyDto;
import yuriy.dev.currencyservice.model.Currency;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {


    Currency toCurrency(CurrencyDto dto);

    CurrencyDto toCurrencyDto(Currency currency);
}
