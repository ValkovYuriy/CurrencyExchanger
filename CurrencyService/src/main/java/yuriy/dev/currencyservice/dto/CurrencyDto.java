package yuriy.dev.currencyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CurrencyDto(
        @Schema(hidden = true)
        UUID id,
        @Schema(description = "Код валюты", example = "USD")
        @NotBlank(message = "Код валюты не может быть пустым")
        String code,
        @Schema(description = "Название валюты", example = "Dollar")
        @NotBlank(message = "Название валюты не может быть пустым")
        String name
) {
}
