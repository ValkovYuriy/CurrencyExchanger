package yuriy.dev.exchangeservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import yuriy.dev.exchangeservice.dto.UserDto;
import yuriy.dev.exchangeservice.model.User;

@Mapper(componentModel = "spring",uses = {RoleMapper.class,DealMapper.class})
public interface UserMapper {

    @Mapping(target = "deals",source = "dealsDto")
    @Mapping(target = "roles",source = "rolesDto")
    User toUser(UserDto userDto);

    @Mapping(target = "dealsDto",source = "deals")
    @Mapping(target = "rolesDto",source = "roles")
    UserDto toUserDto(User user);

}
