package yuriy.dev.exchangeservice.mapper;

import org.mapstruct.Mapper;
import yuriy.dev.dto.RoleDto;
import yuriy.dev.model.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDto toDto(Role role);

    Role toEntity(RoleDto roleDto);
}
