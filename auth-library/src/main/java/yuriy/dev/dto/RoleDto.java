package yuriy.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record RoleDto(
        @Schema(hidden = true)
        UUID id,
        @Schema(description = "Роль пользователя")
        String role
) {
}
