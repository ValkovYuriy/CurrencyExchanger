package yuriy.dev.exchangeservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import yuriy.dev.exchangeservice.dto.DealDto;
import yuriy.dev.exchangeservice.model.Deal;

@Mapper(componentModel = "spring")
public interface DealMapper {

    @Mapping(target = "user", ignore = true)
    Deal toDeal(DealDto dealDto);

    @Mapping(target = "userId", source = "user.id")
    DealDto toDealDto(Deal deal);

}
