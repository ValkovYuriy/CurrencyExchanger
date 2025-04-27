package yuriy.dev.cashbalanceservice.mapper;

import org.mapstruct.Mapper;
import yuriy.dev.cashbalanceservice.dto.CashBalanceDto;
import yuriy.dev.cashbalanceservice.model.CashBalance;

@Mapper(componentModel = "spring")
public interface CashBalanceMapper {

    CashBalance toCashBalance(CashBalanceDto cashBalanceDto);

    CashBalanceDto toCashBalanceDto(CashBalance cashBalance);
}
