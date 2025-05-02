package yuriy.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

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
        List<RoleDto> rolesDto
) {
}
