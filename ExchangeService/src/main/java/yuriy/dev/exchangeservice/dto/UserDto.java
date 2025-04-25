package yuriy.dev.exchangeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record UserDto(
        UUID id,
        @Schema(name = "Имя пользователя")
        String username,
        @Schema(name = "Пароль")
        String password,
        @Schema(name = "Роли пользователя")
        List<RoleDto> rolesDto,
        @Schema(name = "Сделки пользователя")
        List<DealDto> dealsDto
) {
}
