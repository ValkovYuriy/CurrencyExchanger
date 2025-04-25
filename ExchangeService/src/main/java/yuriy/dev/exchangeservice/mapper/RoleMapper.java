package yuriy.dev.exchangeservice.mapper;

import org.mapstruct.Mapper;
import yuriy.dev.exchangeservice.dto.RoleDto;
import yuriy.dev.exchangeservice.model.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toRole(RoleDto roleDto);

    RoleDto toRoleDto(Role role);
}
