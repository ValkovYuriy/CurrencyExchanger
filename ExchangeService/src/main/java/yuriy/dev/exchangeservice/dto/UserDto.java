package yuriy.dev.exchangeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record UserDto(
        @Schema(hidden = true)
        UUID id,
        @Schema(description = "Имя пользователя")
        String username,
        @Schema(description = "Пароль")
        String password,
        @Schema(description = "Роли пользователя")
        List<RoleDto> rolesDto,
        @Schema(description = "Сделки пользователя")
        List<DealDto> dealsDto
) {
}
