package yuriy.dev.exchangeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import yuriy.dev.dto.RoleDto;

import java.util.List;
import java.util.UUID;

@Builder
public record UserDto(
        @Schema(hidden = true)
        UUID id,
        @Schema(description = "Имя пользователя")
        @NotBlank(message = "Имя пользователя не может быть пустым")
        String username,
        @Schema(description = "Пароль")
        @NotBlank(message = "Пароль пользователя не может быть пустым")
        String password,
        @Schema(description = "Роли пользователя")
        List<RoleDto> rolesDto,
        @Schema(description = "Сделки пользователя",hidden = true)
        List<DealDto> dealsDto
) {
}
