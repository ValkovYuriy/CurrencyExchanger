package yuriy.dev.exchangeservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import yuriy.dev.exchangeservice.dto.DealDto;
import yuriy.dev.exchangeservice.model.Deal;

@Mapper(componentModel = "spring")
public interface DealMapper {

    @Mapping(target = "user",source = "userDto")
    Deal toDeal(DealDto dealDto);

    @Mapping(target = "userDto", source = "user")
    DealDto toDealDto(Deal deal);
}
